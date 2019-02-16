FROM python:3.7-alpine3.9

ARG PIP_EXTRA_INDEX_URL
RUN : "${PIP_EXTRA_INDEX_URL?Requires PIP_EXTRA_INDEX_URL}"

ENV S6_SERVICE_PATH=/etc/service.d
RUN mkdir -vp $S6_SERVICE_PATH

WORKDIR /home/app
COPY requirements.txt .
RUN \
  apk add --no-cache --virtual basics \
    bash curl py-openssl \
  && apk add --no-cache --virtual celery-deps libressl-dev libffi-dev \
  && apk add --no-cache --virtual Flask-deps build-base \
  && apk add --no-cache --virtual uWSGI-deps linux-headers pcre pcre-dev \
  && apk add --no-cache \
    s6 \
    nginx \
  && pip install --extra-index-url "$PIP_EXTRA_INDEX_URL" \
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

# See main.py for steps to enable remote debugging
#COPY pycharm-debug-py3k.egg .

EXPOSE 80

CMD ["./run.sh"]