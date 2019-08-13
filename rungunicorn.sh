#! /bin/bash

{
# Run app using gunicorn with gevent and 500 concurrent connections.
cd application && gunicorn -b 0.0.0.0:80 --workers 1 main:app \
    --timeout 90 --log-level=DEBUG --log-file - --error-logfile - --access-logfile - -k gevent --worker-connections 500
}
