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
  version = "0.10.1" # Heredoc strings delimited by commas broken in 0.10.2
}

variable "blue_green_first_deployment" {
  type = string
  default = "false"
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

  blue_green_enable = "true"
  blue_green_first_deployment = var.blue_green_first_deployment
  blue_green_scale_down_delay_seconds = "120"

  service_resource_requests_memory = "128Mi"
  service_resource_limits_memory = "128Mi"
  service_resource_requests_cpu = "100m"
  service_resource_limits_cpu = "100m"
  service_readiness_probe_path = "/health"

  service_environments_variables = <<EOF
    GIT_SHA=localbuild
    ENVIRONMENT=local
   EOF
}
