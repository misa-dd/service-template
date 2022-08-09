from commons import my_service_stub
from locust import events

import time


class GrpcClient():
    def __init__(self, host):
        self.stub = my_service_stub()  # Create your grpc stub here

    def __getattr__(self, name):
        func = self.stub.__getattribute__(name)

        def wrapper(*args, **kwargs):
            start_time = time.time()
            try:
                response = func(*args, **kwargs).SerializeToString()
            except Exception as e:  # noqa
                total_time = int((time.time() - start_time) * 1000)
                events.request_failure.fire(
                    request_type="grpc", name=name, response_time=total_time,
                    exception=e, response_length=0)
                print(e)
            else:
                total_time = int((time.time() - start_time) * 1000)
                events.request_success.fire(
                    request_type="grpc", name=name, response_time=total_time,
                    response_length=len(response))

        return wrapper
