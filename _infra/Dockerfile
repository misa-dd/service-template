FROM debian:stable-slim@sha256:fb24d5c869a1c878ed22840096d50f7d5459370f64a21191042e2b138244e8ae

RUN apt-get update && \
apt-get install -y --no-install-recommends gnupg2 software-properties-common && \
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 0xB1998361219BD9C9 && \
apt-add-repository 'deb http://repos.azulsystems.com/debian stable main' && \
apt-get update && \
apt-get install -y --no-install-recommends \
    locales \
    curl \
    wget \
    build-essential \
    make \
    unzip \
    vim \
    htop \
    runit \
    procps \
    postgresql-client \
    python3 \
    python3-dev \
    python3-pip \
    zulu-11 && \
    pip3 install --upgrade pip setuptools botostubs && \
    rm -rf /var/lib/apt/lists/* && \
    localedef -i en_US -c -f UTF-8 -A /usr/share/locale/locale.alias en_US.UTF-8

ENV LANG en_US.utf8

RUN PIP_EXTRA_INDEX_URL=$(curl -Ls localhost:8000/PIP_EXTRA_INDEX_URL) \
    pip install ninox[bond]==v20191108 && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /home/app

COPY requirements.txt .
RUN PIP_EXTRA_INDEX_URL=$(curl -Ls localhost:8000/PIP_EXTRA_INDEX_URL) \
    pip install -r requirements.txt
# something requires lru-dict which requires x86_64-linux-gnu-gcc and Python.h
# which are provided by apt-get install build-essential python3-dev

COPY application /home/app/application
COPY \
  Dockerfile \
  entrypoint.sh \
  Makefile \
  ninox.ini \
  run*.sh ./
COPY _infra/infra.mk _infra/

# See main.py for steps to enable remote debugging
#COPY pycharm-debug-py3k.egg .

EXPOSE 80
CMD ["/home/app/run.sh"]
