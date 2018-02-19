SHA = $(shell git rev-parse HEAD)
DOCKER_IMAGE_URL="611706558220.dkr.ecr.us-west-2.amazonaws.com/doordash/service-template"
LOCAL_TAG="service-template:localbuild"

.PHONY: build
build:
	docker build . -t ${LOCAL_TAG}

.PHONY: tag
tag:
	$(doorctl) tag --repourl $(DOCKER_IMAGE_URL) --localimage $(LOCAL_TAG) --sha $(SHA) --branch $(branch)

.PHONY: push
push:
	$(doorctl) push --repourl $(DOCKER_IMAGE_URL) --localimage $(LOCAL_TAG) --sha $(SHA) --branch $(branch)

.PHONY: render
render:
	rm -rf build-infra2 || true
	mkdir build-infra2
	cat infra/k8s-templates/app.yaml | BINDMOUNT1=${PWD}/infra/k8s-template-variables/$(kubernetes-cluster)/$(kubernetes-namespace)/$(kubernetes-namespace).yaml:/variables.yaml $(doorctl) exec python /home/app/rendering/cli.py render --yml_variables_file=/variables.yaml --var="git_sha=$(SHA)" > build-infra2/app.yaml

.PHONY: deploy
deploy:
	cat build-infra2/app.yaml
	cat build-infra2/app.yaml | $(doorctl) exec python /home/app/kubernetes/cli.py apply --kubernetes-namespace=$(kubernetes-namespace) --wait-timeout=300

##### APPLICATION SPECIFIC DEPLOY TASKS BELOW
.PHONY: test
test:
	echo "no tests"

.PHONY: dev
dev:
	docker run -d -p 80:5000 -v `pwd`/app:/root/app service bash runlocal.sh

