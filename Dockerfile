FROM ubuntu:latest

RUN apt-get update -y && \
    apt-get install -y \
    python3-pip \
    python3-dev \
    rsyslog

RUN mkdir /app
WORKDIR /app

COPY requirements.txt .

RUN pip3 install -r requirements.txt

COPY . /app/
