import logging
import sys

from doordash_lib.microservice import setup
from doordash_lib.runtime import Runtime
from doordash_lib.stats.ddstats import doorstats_global, doorstats_internal, doorstats
from flask import Flask

from conf import config


app = Flask(__name__)
config.configure_app(app)

logging.basicConfig(stream=sys.stdout, level=logging.INFO)
logger = logging.getLogger('main')

runtime = Runtime(location="/srv/runtime/current", namespace="service-template")

setup.init()


@app.route('/')
def hello_world():
    return 'Hello, World!'


@app.route('/health')
def health():
    return 'OK'


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)
