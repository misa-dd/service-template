import os
import grpc

_GRPC_CHANNEL = None


def grpc_channel():
    global _GRPC_CHANNEL
    service_url = os.getenv('SERVICE_GRPC_URL')
    assert service_url is not None
    _GRPC_CHANNEL = _GRPC_CHANNEL or grpc.insecure_channel(service_url)
    return _GRPC_CHANNEL


def my_service_stub():
    # Use grpc_channel() to create your service stub
    return None


def closeChannel():
    _GRPC_CHANNEL.close()
