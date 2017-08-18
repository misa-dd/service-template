FROM ubuntu:latest

RUN apt-get update -y && \
    apt-get install -y \
    python3-pip \
    python3-dev \
    rsyslog \
    supervisor \
    nginx \
    curl

ENV LC_ALL C.UTF-8

RUN mkdir -p /root
WORKDIR /root

COPY requirements.txt .

RUN pip3 install -r requirements.txt

EXPOSE 5000

COPY . /root/

RUN rm /etc/nginx/nginx.conf
RUN ln -s /root/docker/nginx.conf /etc/nginx/
RUN ln -s /root/docker/nginx-app.conf /etc/nginx/conf.d/

RUN cp /root/docker/supervisor*.conf /etc/supervisor/conf.d/

CMD ["/root/docker/run.sh"]
