ARG IMAGE_SERVICE=611706558220.dkr.ecr.us-west-2.amazonaws.com/dd-docker-base:python3-latest
FROM ${IMAGE_SERVICE}

# Install requirements in /home/app
WORKDIR /home/app

# for kaniko
COPY requirements.txt .
RUN PIP_EXTRA_INDEX_URL=$(cat /kaniko/secrets/PIP_EXTRA_INDEX_URL) pip install -r requirements.txt

# Copy to /home/app
WORKDIR /home/app
COPY \
  Dockerfile \
  entrypoint.sh \
  Makefile \
  run*.sh ./

# See main.py for steps to enable remote debugging
#COPY pycharm-debug-py3k.egg .

# Copy to /home/app/application
WORKDIR /home/app/application
COPY application .

# Copy to /home/app/_infra
WORKDIR /home/app/_infra
COPY _infra/infra.mk .

WORKDIR /home/app

WORKDIR /home/app
EXPOSE 80
CMD ["/home/app/run.sh"]
