# NOTE: SERVICE OWNERS SHOULD GENERALLY NOT NEED TO MODIFY THIS FILE.
# Instead, place your tasks in the root Makefile
SHA = $(shell git rev-parse HEAD)
DOCKER_IMAGE_URL="611706558220.dkr.ecr.us-west-2.amazonaws.com/doordash/service-template"
LOCAL_TAG="service-template:localbuild"

.PHONY: docker-build
docker-build:
	docker build . -t ${LOCAL_TAG} --build-arg PIP_EXTRA_INDEX_URL=$(PIP_EXTRA_INDEX_URL)

.PHONY: tag
tag:
	$(doorctl) tag --repourl $(DOCKER_IMAGE_URL) --localimage $(LOCAL_TAG) --sha $(SHA) --branch $(branch)

.PHONY: push
push:
	$(doorctl) push --repourl $(DOCKER_IMAGE_URL) --localimage $(LOCAL_TAG) --sha $(SHA) --branch $(branch)
