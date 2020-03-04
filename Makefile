include _infra/infra*.mk

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

.PHONY: pulse-clean
pulse-clean:
	rm -rf /tmp/pulsevenv pulse/.pytest_cache pulse/report*

.PHONY: pressure-test
pressure-test:
	./local-pressure.sh

.PHONY: pressure-clean
pressure-clean:
	rm -rf /tmp/pressurevenv pressure/.pytest_cache pressure/report*

.PHONY: clean
clean: stop local-clean pulse-clean pressure-clean
	./gradlew clean
	rm -rf out
