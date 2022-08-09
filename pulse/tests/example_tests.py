# This test file will be ignored. Don't need to keep around if you have
# mastered the art of Pulse testing.
import grpc
import pytest

from doordash_pulse import *  # noqa


@retry((IOError,))  # noqa
def get_token_info(client, token_info):
    client.GetTokenInfo(request=token_info)


@latency_test(2)  # noqa
def test_my_request_latency(identity_client, identity_token_info):
    """ Tests end-to-end latency of an identity service GRPC endpoint

    Will fail if the test takes more than 2 seconds to execute

    :param identity_client: GRPC identity service client
    :param identity_token_info: identity service Token Info object
    """
    identity_token_info.service_id = 'someserviceid'
    with pytest.raises(grpc.RpcError):
        get_token_info(identity_client, identity_token_info)
