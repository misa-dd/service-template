FROM ubuntu:bionic

RUN apt-get update && \
apt-get install -y --no-install-recommends gnupg2 software-properties-common && \
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 0xB1998361219BD9C9 && \
apt-add-repository 'deb http://repos.azulsystems.com/ubuntu stable main' && \
apt-get install -y --no-install-recommends \
    locales \
    curl \
    wget \
    build-essential \
    make \
    htop \
    runit \
    postgresql-client \
    python3 \
    python3-dev \
    python3-pip \
    zulu-11 && \
    pip3 install --upgrade pip setuptools botostubs && \
    rm -rf /var/lib/apt/lists/* && \
    localedef -i en_US -c -f UTF-8 -A /usr/share/locale/locale.alias en_US.UTF-8

ARG PIP_EXTRA_INDEX_URL
ENV LANG en_US.utf8
RUN : "${PIP_EXTRA_INDEX_URL?Requires PIP_EXTRA_INDEX_URL}"
RUN pip install doordash-secret==0.0.30 && \
    pip install ninox==v20190810 && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /home/app
COPY requirements.txt .
RUN pip install -r requirements.txt
# something requires lru-dict which requires x86_64-linux-gnu-gcc and Python.h
# which are provided by apt-get install build-essential python3-dev

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

CMD ["/home/app/run.sh"]
