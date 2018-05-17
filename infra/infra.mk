# INFRA HEADER -- DO NOT EDIT. metadata: {"checksum": "3e41ce0e526af34042ae82e7b932084c"}
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

.PHONY: render
render:
	rm -rf build-infra2
	mkdir build-infra2
	awk 'FNR==1{print ""}1' infra/k8s-template-variables/$(kubernetes-cluster)/$(kubernetes-namespace)/*.yaml > build-infra2/variables.yaml
	cat infra/k8s-templates/*.yaml | BINDMOUNT1=${PWD}/build-infra2/variables.yaml:/variables.yaml $(doorctl) exec python /home/app/rendering/cli.py render $(maybe-istio) --yml_variables_file=/variables.yaml --var="git_sha=$(SHA)" > build-infra2/deploy.yaml

.PHONY: deploy
deploy:
	cat build-infra2/deploy.yaml
	cat build-infra2/deploy.yaml | $(doorctl) exec python /home/app/kubernetes/cli.py apply --kubernetes-namespace=$(kubernetes-namespace) --wait-timeout=300