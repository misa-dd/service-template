# service template

Reference project for setting up a service to run on doordash infra 2.0

# terraform

Look at examples in https://github.com/doordash/service-config-examples for help writing terraform templates to `infra/tf/`. Then, render the templates and apply:

```
make render-tf
make apply-tf
```

# local development

```
make build
make dev
```
