provider "archive" {
  version = "1.2.2"
}

provider "kubernetes" {
  version = "1.8.1"
}

provider "helm" {
  kubernetes {
    config_context = "docker-for-desktop"
  }
  install_tiller = false
  version = "0.10.4" # Heredoc strings delimited by commas broken in 0.10.2
}

variable "blue_green_first_deployment" {
  type = string
  default = "false"
}

variable "git_sha" {
  type = string
  default = "localbuild"
}

module "service" {
  source = "git::https://github.com/doordash/terraform-kubernetes-microservice.git?ref=master"

  namespace                 = "service-template"
  service_name              = "service-template"
  service_app               = "web"
  service_contact_info      = "eng-devprod@doordash.com"
  service_docker_image      = "service-template"
  service_docker_image_tag  = "localbuild"
  runtime_enable            = "false"
  net_service_type          = "LoadBalancer"
  service_image_pull_policy = "IfNotPresent"
  service_cmd               = "/home/app/run.sh"
  service_cmd_args          = ""
  service_replica_count     = "1"

  service_container_port = "80"
  net_service_port = "80"

  helm_timeout = "0"
  blue_green_enable = "true"
  blue_green_first_deployment = var.blue_green_first_deployment
  blue_green_scale_down_delay_seconds = "120"

  service_resource_requests_memory = "128Mi"
  service_resource_limits_memory = "128Mi"
  service_resource_requests_cpu = "100m"
  service_resource_limits_cpu = "100m"
  service_readiness_probe_path = "/health"

  service_environments_variables = <<EOF
    GIT_SHA=${var.git_sha}
    ENVIRONMENT=local
   EOF
}

module "worker" {
  source = "git::https://github.com/doordash/terraform-kubernetes-microservice.git?ref=master"

  namespace                 = "service-template"
  service_name              = "service-template"
  service_app               = "worker"
  service_contact_info      = "eng-devprod@doordash.com"
  service_docker_image      = "service-template"
  service_docker_image_tag  = "localbuild"
  runtime_enable            = "false"
  service_image_pull_policy = "IfNotPresent"
  service_cmd               = "/home/app/run.sh"
  service_cmd_args          = ""
  service_replica_count     = "1"

  helm_timeout = "0"
  blue_green_enable = "false"
  net_service_enable = "false"

  service_resource_requests_memory = "128Mi"
  service_resource_limits_memory = "128Mi"
  service_resource_requests_cpu = "100m"
  service_resource_limits_cpu = "100m"

  service_environments_variables = <<EOF
    GIT_SHA=${var.git_sha}
    ENVIRONMENT=local
   EOF
}

module "cronjob" {
  source = "git::https://github.com/doordash/terraform-kubernetes-microservice.git?ref=master"

  namespace                 = "service-template"
  service_name              = "service-template"
  service_app               = "cronjob"
  service_contact_info      = "eng-devprod@doordash.com"
  service_docker_image      = "service-template"
  service_docker_image_tag  = "localbuild"
  runtime_enable            = "false"
  service_image_pull_policy = "IfNotPresent"
  service_cmd               = "/home/app/entrypoint.sh"
  service_cmd_args          = "/home/app/run-job.sh"

  helm_timeout = "0"
  job_enable = "true"
  job_cron_schedule = "* * * * *"

  service_resource_requests_memory = "128Mi"
  service_resource_limits_memory = "128Mi"
  service_resource_requests_cpu = "100m"
  service_resource_limits_cpu = "100m"

  service_environments_variables = <<EOF
    GIT_SHA=${var.git_sha}
    ENVIRONMENT=local
    JOB_NAME=cronjob
    DATABASE_PASSWORD=todo-add-to-secrets
   EOF
 
  service_custom_pod_annotations = <<EOF
    chronosphere.io/job: "service-template"
    chronosphere.io/scrape: "true"
    chronosphere.io/port: “80”
    chronosphere.io/path: “/metrics”
  EOF

}
