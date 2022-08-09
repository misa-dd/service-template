ARG PULSE_VERSION
FROM 611706558220.dkr.ecr.us-west-2.amazonaws.com/doordash-pulse:${PULSE_VERSION}

ARG SERVICE_NAME
RUN : "${SERVICE_NAME?Requires SERVICE_NAME}"
ENV SERVICE_NAME ${SERVICE_NAME}

ARG ARTIFACTORY_USERNAME
ARG ARTIFACTORY_PASSWORD

COPY . /pulse/
WORKDIR /pulse

RUN python3.6 -m pip install  -r requirements.txt

############################################################################################
#                                                                                          #
# Please add any custom command e.g. copy directories, define environment variables, etc.  #
#                                    BELOW THIS SECTION                                    #
# (docs at https://github.com/doordash/doordash-pulse/blob/master/README.md#dockerfile     #
#                                                                                          #
############################################################################################
ENV PROTOCOL_BUFFERS_PYTHON_IMPLEMENTATION='python'
