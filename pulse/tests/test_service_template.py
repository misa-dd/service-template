import requests
import os

SERVICE_URI = os.getenv('SERVICE_URI')


def test_health_api():
    health_api_uri = '{0}/health'.format(SERVICE_URI)
    r = requests.get(health_api_uri)

    assert r.status_code == 200
    assert r.text == 'OK'


def test_service_api():
    service_uri = '{0}/'.format(SERVICE_URI)
    r = requests.get(service_uri)

    assert r.status_code == 200
    assert r.text.startswith('Hello, World! I am running version ')


def test_service_api_with_name():
    name = "Pulse"
    service_uri = '{0}/?name={1}'.format(SERVICE_URI, name)
    r = requests.get(service_uri)

    assert r.status_code == 200
    assert r.text.startswith('Hello, {0}! I am running version '.format(name))
