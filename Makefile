build:
	docker build . -t service

test:
	echo "no tests"

dev:
	docker run -d -p 80:5000 -v `pwd`/app:/root/app service bash runlocal.sh

deployvenv: infra/deploy/requirements.txt
	cd infra/deploy; test -d venv || virtualenv venv
	cd infra/deploy; venv/bin/pip install -Ur requirements.txt

login:
	$(infra/deploy/venv/bin/aws ecr get-login --no-include-email --region us-west-2)

# Call pattern:
# make dockerbuildtagpush
dockerbuildtagpush: deployvenv login
	cd infra/deploy; venv/bin/python ./docker-build-tag-push.py

# Call pattern:
# make fabric="staging" vars="git_sha=8b7f2bc88d13473dcd9271dcaea2a8038d1e3ad5" k8-credentials-file="/usr/local/doordash/kubernetes/config.staging" deploy
deploy: deployvenv login
	cd infra/deploy; venv/bin/python ./deploy.py --fabric $(fabric) --k8-credentials-file $(k8-credentials-file) --vars $(vars)
