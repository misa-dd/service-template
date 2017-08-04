import os
import sys
import yaml
from jinja2 import Environment, FileSystemLoader


def render(src, dst, context):
    env = Environment(loader=FileSystemLoader(src))
    for root, _, files in os.walk(src):
        for name in files:
            relpath = os.path.relpath(root, start=src)
            out = env.get_template(os.path.join(relpath, name)).render(**context)
            dst_path = os.path.join(dst, relpath)
            mkdirp(dst_path)
            with open(os.path.join(dst_path, name), 'w') as f:
                f.write(out)


def mkdirp(path):
    try:
        os.makedirs(path)
    except OSError:
        if not os.path.isdir(path):
            raise


if __name__ == '__main__':
    src, dst, env = sys.argv[1:]
    with open("infra/env/{}.yaml".format(env)) as f:
        context = yaml.load(f)
    render(src, dst, context)
