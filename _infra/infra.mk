# NOTE: SERVICE OWNERS SHOULD GENERALLY NOT NEED TO MODIFY THIS FILE.
# Instead, place your tasks in the root Makefile
SHA = $(shell git rev-parse HEAD)
SERVICE_NAME=service-template
APP=web
DOCKER_IMAGE_URL=611706558220.dkr.ecr.us-west-2.amazonaws.com/$(SERVICE_NAME)
LOCAL_TAG=$(SERVICE_NAME):localbuild
LOCAL_CHART=_infra/charts/$(SERVICE_NAME)

ifeq ($(SECRETS),)
  SECRETS=env.SECRETS=none
endif

ifeq ($(CACHE_FROM),)
  CACHE_FROM=$(LOCAL_TAG)
endif

.PHONY: docker-build
docker-build:
	docker build . -t $(LOCAL_TAG) --cache-from $(CACHE_FROM) --build-arg "PIP_EXTRA_INDEX_URL=$(PIP_EXTRA_INDEX_URL)"

.PHONY: local-deploy
local-deploy:
	cd _infra/local && \
	rm -rf .terraform apply.tfplan && \
	terraform init && \
	terraform plan -out apply.tfplan && \
	terraform apply apply.tfplan

.PHONY: local-status
local-status:
	helm status $(SERVICE_NAME)-$(APP)

.PHONY: local-clean
local-clean:
	helm delete --purge $(SERVICE_NAME)-$(APP)

.PHONY: local-tail
local-tail:
	kubectl get pods -n $(SERVICE_NAME) -l service=$(SERVICE_NAME) -l app=$(APP) -o jsonpath="{.items[0].metadata.name}" | xargs kubectl logs -n $(SERVICE_NAME) -f --tail=10

.PHONY: tag
tag:
	$(doorctl) tag --repourl $(DOCKER_IMAGE_URL) --localimage $(LOCAL_TAG) --sha $(SHA) --branch $(branch)

.PHONY: push
push:
	$(doorctl) push --repourl $(DOCKER_IMAGE_URL) --localimage $(LOCAL_TAG) --sha $(SHA) --branch $(branch)

.PHONY: remove
remove:
	docker images -a --filter=reference="$(DOCKER_IMAGE_URL):$(SHA)" --format "{{.ID}}" | sort | uniq | xargs docker rmi -f

