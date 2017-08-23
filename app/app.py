import os

from doordash_lib.stats.ddstats import doorstats_global, doorstats
from flask import Flask


app = Flask(__name__)


statsd_server = os.environ.get('INSTANCE_LOCAL_IP', 'prod-proxy-internal.doordash.com')
# TODO (bliang) does it make sense to have more hierarchy here than completely flat by namespace?
STATS_PREFIX = "{}.{}_{}".format(os.environ['HOSTNAME'], os.environ['SERVICE_NAME'], os.environ['FABRIC'])

doorstats.initialize(host=statsd_server, prefix=STATS_PREFIX)
doorstats_global.initialize(host=statsd_server, prefix=STATS_PREFIX)


@app.route("/")
def hello():
    doorstats_global.incr('hello')
    return "Hello World!"
