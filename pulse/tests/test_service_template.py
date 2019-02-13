import requests
import os

SERVICE_URI = os.getenv('SERVICE_URI')


def test_health_api():
    health_api_uri = '{0}/health'.format(SERVICE_URI)
    r = requests.get(health_api_uri)

    assert r.status_code == 200
    assert r.text == 'OK'
