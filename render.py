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
    src, dst, env = sys.argv[1:]
    with open("infra/env/{}.yaml".format(env)) as f:
        context = yaml.load(f)
    context['aws_account'] = aws('sts', 'get-caller-identity', '--output', 'text', '--query', 'Account').strip()
    context['git_sha'] = git('rev-parse', 'HEAD')
    render(src, dst, context)
