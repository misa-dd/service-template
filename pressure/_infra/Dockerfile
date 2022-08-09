ARG PRESSURE_VERSION
FROM 611706558220.dkr.ecr.us-west-2.amazonaws.com/doordash-pressure:${PRESSURE_VERSION}

ARG SERVICE_NAME
ENV SERVICE_NAME ${SERVICE_NAME}

COPY . /pressure/
WORKDIR /pressure

RUN ARTIFACTORY_USERNAME=$(cat /kaniko/secrets/ARTIFACTORY_USERNAME) \
    ARTIFACTORY_PASSWORD=$(cat /kaniko/secrets/ARTIFACTORY_PASSWORD) \
    python3.6 -m pip install  -r requirements.txt

###############################################################################################################
#                                                                                                             #
# Please add any custom command e.g. copy directories, define environment variables, etc.                     #
#                                         BELOW THIS SECTION                                                  #
# (docs at https://github.com/doordash/doordash-pressure/blob/master/README.md#dockerfile)                    #
#                                                                                                             #
###############################################################################################################
