FROM python:3.6.4-alpine3.7

ARG PIP_EXTRA_INDEX_URL
RUN : "${PIP_EXTRA_INDEX_URL?Requires PIP_EXTRA_INDEX_URL}"

ENV S6_SERVICE_PATH=/etc/service.d
RUN mkdir -vp $S6_SERVICE_PATH

WORKDIR /home/app
COPY requirements.txt .
RUN \
  apk add --no-cache --virtual basics \
    bash \
  && apk add --no-cache --virtual celery-deps curl-dev libressl-dev \
  && apk add --no-cache --virtual Flask-deps build-base \
  && apk add --no-cache --virtual uWSGI-deps linux-headers pcre pcre-dev \
  && apk add --no-cache \
    s6 \
    nginx \
  && pip install --extra-index-url $PIP_EXTRA_INDEX_URL \
    -r requirements.txt \
  && rm -v \
    /etc/nginx/nginx.conf \
    /etc/nginx/conf.d/default.conf \
  && apk del --quiet Flask-deps uWSGI-deps

COPY root-fs/ /
COPY application /home/app/application
COPY \
  Dockerfile \
  entrypoint.sh \
  run*.sh ./


EXPOSE 80

CMD ["./run.sh"]