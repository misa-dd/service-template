FROM ubuntu:latest

RUN apt-get update -y && \
    apt-get install -y \
    python3-pip \
    python3-dev \
    rsyslog

ENV LC_ALL C.UTF-8

RUN mkdir /app
WORKDIR /app

COPY requirements.txt .

RUN pip3 install -r requirements.txt

EXPOSE 5000

COPY . /app/
