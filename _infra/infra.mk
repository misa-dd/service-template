# NOTE: SERVICE OWNERS SHOULD GENERALLY NOT NEED TO MODIFY THIS FILE.
# Instead, place your tasks in the root Makefile
SHA=$(shell git rev-parse HEAD)
SERVICE_NAME=service-template
APP=web
NAMESPACE=$(SERVICE_NAME)
DOCKER_IMAGE_URL=611706558220.dkr.ecr.us-west-2.amazonaws.com/$(SERVICE_NAME)
LOCAL_TAG=$(SERVICE_NAME):localbuild

ifeq ($(CACHE_FROM),)
  CACHE_FROM=$(LOCAL_TAG)
endif

.PHONY: docker-build
docker-build:
	docker build . -t $(LOCAL_TAG) \
	--cache-from $(CACHE_FROM) \
	--build-arg PIP_EXTRA_INDEX_URL="$(PIP_EXTRA_INDEX_URL)" \
	--build-arg ARTIFACTORY_PASSWORD="$(ARTIFACTORY_PASSWORD)" \
	--build-arg ARTIFACTORY_USERNAME="$(ARTIFACTORY_USERNAME)"

.PHONY: local-deploy
local-deploy:
	cd _infra/local && \
	terraform init && \
	terraform plan -out apply.tfplan && \
	terraform apply apply.tfplan

.PHONY: local-status
local-status:
	helm status $(SERVICE_NAME)-$(APP)

.PHONY: local-clean
local-clean:
	cd _infra/local && terraform destroy -auto-approve || true
	helm --kube-context docker-for-desktop delete --purge $(SERVICE_NAME)-$(APP) || true
	rm -rf _infra/local/.terraform _infra/local/terraform.tfstate* _infra/local/apply.tfplan

.PHONY: local-describe
local-describe:
	kubectl describe -n $(NAMESPACE) pod `kubectl get pods -n $(NAMESPACE) -l service=$(SERVICE_NAME) -l app=$(APP) -o jsonpath="{.items[0].metadata.name}"`

.PHONY: local-tail
local-tail:
	kubectl logs -n $(NAMESPACE) -f --tail=10 `kubectl get pods -n $(NAMESPACE) -l service=$(SERVICE_NAME) -l app=$(APP) -o jsonpath="{.items[0].metadata.name}"` $(APP)

.PHONY: local-bash
local-bash:
	kubectl exec -n $(NAMESPACE) -it `kubectl get pods -n $(NAMESPACE) -l service=$(SERVICE_NAME) -l app=$(APP) -o jsonpath="{.items[0].metadata.name}"` --container=$(APP) bash

.PHONY: local-port-forward
local-port-forward:
	kubectl -n $(NAMESPACE) port-forward svc/$(SERVICE_NAME)-$(APP) 7001:80

.PHONY: tag
tag:
	docker tag $(LOCAL_TAG) $(DOCKER_IMAGE_URL):$(SHA)

.PHONY: push
push:
	docker push $(DOCKER_IMAGE_URL):$(SHA)

.PHONY: remove-docker-images
remove-docker-images:
	docker images -a --filter=reference="$(DOCKER_IMAGE_URL):$(SHA)" --format "{{.ID}}" | sort | uniq | xargs -r docker rmi -f

.PHONY: migrate
migrate:
	@echo "Migrated $(SERVICE_NAME) to $(tag) for $(env) within $(k8sNamespace) on $(k8sCluster)"
