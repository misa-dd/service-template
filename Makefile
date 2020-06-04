include _infra/infra*.mk

# Include environment file if ENVFILE was passed in when invoking make
-include ${ENVFILE}
export $(shell [ -e "$(ENVFILE)" ] && sed 's/=.*//' "$(ENVFILE)")

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

.PHONY: clean
clean: stop local-clean
	find . -name __pycache__ -type d | xargs rm -rf || true
