#!/usr/bin/python3

# To debug a Docker container remotely...
# 1. In PyCharm menu, click Run > Edit Configurations... > + > Python Remote Debug
# 2. Add a path mapping from /Users/.../Projects/risk-service to /home/app
# 3. Uncheck "Suspend  after connect"
# 4. Copy the PyCharm Python3 debug egg: cp -rfp /Applications/PyCharm.app/Contents/debug-eggs/pycharm-debug-py3k.egg .
# 5. Uncomment the "#COPY pycharm-debug-py3k.egg ." line in the Dockerfile file.
# 6. Set "master = false" in the uwsgi.ini file.
# 7. Start your Python Remote Debug server and take note of its port.
# 8. Uncomment the next 4 code lines and update the IP to your laptops IP and the port to the debug server port.
# 9. Build and Deploy: make docker-build local-deploy
#import sys
#sys.path.append('/home/app/pycharm-debug-py3k.egg')
#import pydevd
#pydevd.settrace('172.16.60.178', port=53061, stdoutToServer=True, stderrToServer=True, suspend=False)

import logging
import sys

from doordash_lib.microservice import setup
from doordash_lib.runtime import Runtime
from doordash_lib.stats.ddstats import doorstats_global, doorstats_internal, doorstats
from flask import Flask, request

from conf import config


app = Flask(__name__)
config.configure_app(app)

logging.basicConfig(stream=sys.stdout, level=logging.INFO)
logger = logging.getLogger('main')

runtime = Runtime(location="/srv/runtime/current", namespace="service-template")

setup.init()


@app.route('/')
def hello_world():
    name = request.args.get('name')
    if name is None:
        return 'Salutation, World!'
    else:
        return 'Hi, {0}'.format(name)


@app.route('/health')
def health():
    return 'OK'


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)
