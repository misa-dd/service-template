include _infra/infra*.mk

.PHONY: build
build:
	docker-compose build --build-arg PIP_EXTRA_INDEX_URL="${PIP_EXTRA_INDEX_URL}"

.PHONY: network
network:
	docker network create dr-who || true

.PHONY: run
run: network
	docker-compose up -d

.PHONY: stop
stop:
	docker-compose down

.PHONY: test
test:
	echo "no tests"

.PHONY: pulse-test
pulse-test:
	./local-pulse.sh

.PHONY: pressure-test
pressure-test:
	./local-pressure.sh
