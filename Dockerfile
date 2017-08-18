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

WORKDIR /root

COPY requirements.txt .

RUN pip3 install -r requirements.txt

EXPOSE 5000

COPY . /root/

RUN rm /etc/nginx/nginx.conf
RUN ln -s /root/docker/nginx.conf /etc/nginx/
RUN ln -s /root/docker/nginx-app.conf /etc/nginx/conf.d/

RUN cp /root/docker/supervisor*.conf /etc/supervisor/conf.d/

WORKDIR /opt
RUN curl http://download.splunk.com/products/splunk/releases/6.5.2/universalforwarder/linux/splunkforwarder-6.5.2-67571ef4b87d-Linux-x86_64.tgz | tar -xz
COPY vendor/splunkclouduf.spl /tmp/
RUN /opt/splunkforwarder/bin/splunk start --accept-license --no-prompt && \
    /opt/splunkforwarder/bin/splunk install app /tmp/splunkclouduf.spl -auth admin:changeme

WORKDIR /root

CMD ["/root/docker/run.sh"]
