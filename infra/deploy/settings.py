import os
import yaml

# Docker image host where images are stored
DOCKER_IMAGE_HOST = "611706558220.dkr.ecr.us-west-2.amazonaws.com"

# Root dir of microservice
APP_ROOT = os.path.realpath(
    os.path.dirname(os.path.abspath(__file__)) + "/../../"
)

def get_images_configuration():
	# Return a dictionary that describes the images that should be built
	# as part of a deploy.
	with open('{}/infra/docker/images_configuration.yaml'.format(APP_ROOT), 'r') as yml_file_stream:
		return yaml.load(yml_file_stream)
