build:
	docker build . -t service

dev:
	docker run -d -p 80:5000 -v `pwd`/app:/app/app service bash runlocal.sh
