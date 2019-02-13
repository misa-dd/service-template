#! /bin/bash

# TODO (bliang) any cleaner way of selectively running scandirs in $S6_SERVICE_PATH?
find $S6_SERVICE_PATH/* | egrep -v "(nginx|uwsgi)" | xargs rm -rf
exec /bin/s6-svscan $S6_SERVICE_PATH
