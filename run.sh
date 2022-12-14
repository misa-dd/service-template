#!/bin/bash

cd /home/app/application

# Run app using gunicorn with gevent and 500 concurrent connections.
exec gunicorn -b 0.0.0.0:80 --workers 1 main:app \
    --timeout 90 --log-level=DEBUG --log-file - --error-logfile - --access-logfile - -k gevent --worker-connections 500

# Run app using 2 workers and ERROR log level to realize maximum throughput.
#exec gunicorn -b 0.0.0.0:80 --workers 2 main:app \
#    --timeout 90 --log-level=ERROR --log-file - --error-logfile - --access-logfile - -k gevent --worker-connections 500
