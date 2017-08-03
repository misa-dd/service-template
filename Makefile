build:
	docker build . -t service

dev:
	docker run -d -p 80:5000 service bash runlocal.sh
