# NOTE: SERVICE OWNERS SHOULD GENERALLY NOT NEED TO MODIFY THIS FILE.
# Instead, place your tasks in the root Makefile
SHA = $(shell git rev-parse HEAD)
SERVICE_NAME=service-template
DOCKER_IMAGE_URL=611706558220.dkr.ecr.us-west-2.amazonaws.com/doordash/${SERVICE_NAME}
LOCAL_TAG=${SERVICE_NAME}:localbuild
LOCAL_CHART=_infra/charts/${SERVICE_NAME}

ifeq (${SECRETS},)
  SECRETS=env.SECRETS=none
endif

.PHONY: docker-build
docker-build:
	docker build . -t ${LOCAL_TAG} --build-arg "PIP_EXTRA_INDEX_URL=$(PIP_EXTRA_INDEX_URL)"

.PHONY: local-deploy
local-deploy:
	helm upgrade ${SERVICE_NAME} ${LOCAL_CHART} -i -f ${LOCAL_CHART}/values-local.yaml --set web.runtime.hostPath=${RUNTIME_PATH} --set ${SECRETS}

.PHONY: local-status
local-status:
	helm status ${SERVICE_NAME}

.PHONY: local-clean
local-clean:
	helm delete --purge ${SERVICE_NAME}

.PHONY: local-tail
local-tail:
	kubectl get pods -l service=${SERVICE_NAME} -o jsonpath="{.items[0].metadata.name}" | xargs kubectl logs -f --tail=10

.PHONY: tag
tag:
	$(doorctl) tag --repourl $(DOCKER_IMAGE_URL) --localimage $(LOCAL_TAG) --sha $(SHA) --branch $(branch)

.PHONY: push
push:
	$(doorctl) push --repourl $(DOCKER_IMAGE_URL) --localimage $(LOCAL_TAG) --sha $(SHA) --branch $(branch)
