#!venv/bin/python
import os

import settings
from doordash_cicd.lib import git
from doordash_cicd.step.docker_build_tag_push import perform_step as docker_build_tag_push

if __name__ == "__main__":
    doc = settings.get_images_configuration()
    for container_name, container_config in doc.get("containers", {}).iteritems():
        docker_build_tag_push(
            "{}/{}".format(settings.DOCKER_IMAGE_HOST, container_config.get("ecrName")),
            git.get_sha(),
            git.get_branch(),
            settings.APP_ROOT,
            ["PIP_EXTRA_INDEX_URL={}".format(os.getenv("PIP_EXTRA_INDEX_URL"))]
        )
