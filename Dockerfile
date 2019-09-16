FROM python:3.7-alpine3.9

ARG PIP_EXTRA_INDEX_URL
RUN : "${PIP_EXTRA_INDEX_URL?Requires PIP_EXTRA_INDEX_URL}"

WORKDIR /home/app
COPY requirements.txt .
RUN \
  apk add --no-cache --virtual basics \
    bash curl py-openssl make \
  && apk add --no-cache --virtual celery-deps libressl-dev libffi-dev \
  && apk add --no-cache --virtual Flask-deps build-base \
  && apk add --no-cache --virtual linux-headers pcre pcre-dev \
  && pip install --extra-index-url "$PIP_EXTRA_INDEX_URL" \
    -r requirements.txt \
  && apk del --quiet Flask-deps

COPY application /home/app/application
COPY \
  Dockerfile \
  entrypoint.sh \
  Makefile \
  run*.sh ./
COPY _infra/infra.mk _infra/

# See main.py for steps to enable remote debugging
#COPY pycharm-debug-py3k.egg .

EXPOSE 80

CMD ["./run.sh"]
