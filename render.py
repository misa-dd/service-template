import argparse
import functools
from io import StringIO
import logging
import os
import sh
import sys
import yaml
from jinja2 import Environment, FileSystemLoader


def shwrapper(func, *args, **kwargs):
    capture_output = kwargs.pop('capture_output', True)
    if capture_output:
        buf = StringIO()
        func(_out=buf, *args, **kwargs)
        output = buf.getvalue()
        logging.info(output)
        return output
    else:
        func(*args, **kwargs)


for func in ['aws', 'git']:
    locals()[func] = functools.partial(shwrapper, getattr(sh, func))


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
    # TODO (bliang/jons) all this should eventually be superseded by doorctl
    parser = argparse.ArgumentParser(description='Process some integers.')
    parser.add_argument('--src', help='source directory', type=str, default='infra/k8s')
    parser.add_argument('--dst', help='destination directory', type=str, default='.tmp')
    parser.add_argument('--fabric', help='fabric', type=str)
    parser.add_argument('--aws-account-id', help='AWS account id (if known)', type=str, required=False)
    args = parser.parse_args()

    aws_account_id = args.aws_account_id or aws('sts', 'get-caller-identity', '--output', 'text', '--query', 'Account').strip()
    with open("infra/fabric/{}.yaml".format(args.fabric)) as f:
        context = yaml.load(f)
    context['aws_account_id'] = aws_account_id
    context['git_sha'] = git('rev-parse', 'HEAD')
    render(args.src, args.dst, context)
