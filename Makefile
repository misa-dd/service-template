build:
	docker build . -t service

test:
	echo "no tests"

dev:
	docker run -d -p 80:5000 -v `pwd`/app:/root/app service bash runlocal.sh

render-tf:
	rm -rf .tmp/tf
	mkdir -p .tmp/tf
	docker run -v `pwd`:/root service python3 render.py infra/tf .tmp/tf $(ENV)

apply-tf:
	cd .tmp/tf && terraform init && terraform apply

destroy-tf:
	cd .tmp/tf && terraform init && terraform destroy
