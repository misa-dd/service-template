#! /bin/bash

{
cd application && uwsgi --http :5000 --wsgi-file main.py --callable app
}
