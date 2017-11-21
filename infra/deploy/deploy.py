#!venv/bin/python
import argparse
import os

import settings
from doordash_cicd.step.deploy import perform_step as perform_deploy_step

if __name__ == "__main__":

    parser = argparse.ArgumentParser()
    parser.add_argument("--fabric", required=True)
    parser.add_argument("--k8-credentials-file", metavar="k8_credentials_file", required=True)
    parser.add_argument('--vars',
                        help='Pass name=value pairs to add to template variables when rendering the app.yaml file.',
                        nargs='+', default=[])
    args = parser.parse_args()

    image_url = "{}/{}".format(
        settings.DOCKER_IMAGE_HOST,
        settings.get_images_configuration().get("containers").get("restapi").get("ecrName")
    )

    args_dict = {}
    for name_val_pair in args.vars:
        args_dict[name_val_pair.split("=")[0]] = name_val_pair.split("=")[1]
    args_dict['docker_image_url'] = image_url

    yml_variables_file = None
    if os.path.isfile("{}/infra/fabric/{}.yaml".format(settings.APP_ROOT, args.fabric)):
        yml_variables_file = "{}/infra/fabric/{}.yaml".format(settings.APP_ROOT, args.fabric)

    perform_deploy_step(
        settings.APP_ROOT,
        args.fabric,
        args.k8_credentials_file,
        args_dict,
        yml_variables_file_path=yml_variables_file,
        app_yml_file_path="{}/infra/k8s/app.yaml".format(settings.APP_ROOT)
    )
