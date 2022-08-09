import os
import urllib

from flask import Flask


app = Flask(__name__)


def configure_app(app):
    environment = os.getenv('ENVIRONMENT', '')
    if not environment:
        return
    conf_dir = os.path.join(app.root_path, 'conf')
    for fn in os.listdir(conf_dir):
        if fn.startswith(environment):
            app.config.from_pyfile('{}/{}'.format(conf_dir, fn))


def inject_aws_keys(string):
    string = string.replace('{AWS_ACCESS_KEY_ID}', urllib.parse.quote(os.getenv('AWS_ACCESS_KEY_ID', 'unknown'), safe=''))
    string = string.replace('{AWS_SECRET_ACCESS_KEY}', urllib.parse.quote(os.getenv('AWS_SECRET_ACCESS_KEY', 'unknown'), safe=''))
    return string