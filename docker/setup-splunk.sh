#!/bin/bash

touch /var/log/syslog
/opt/splunkforwarder/bin/splunk add monitor /var/log/ -index {{index_name}} -sourcetype syslog -auth admin:changeme
sed -i "s/host = .*/host = ${FABRIC}/g" /opt/splunkforwarder/etc/system/local/inputs.conf
