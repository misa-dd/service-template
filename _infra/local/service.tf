provider "helm" {
  kubernetes {
    config_context = "docker-for-desktop"
  }
  install_tiller = false
}

module "service" {
  source = "git::https://github.com/doordash/terraform-kubernetes-microservice.git?ref=master"

  namespace                 = "service-template"
  service_name              = "service-template"
  service_app               = "web"
  service_contact_info      = "pe@doordash.com"
  service_docker_image      = "service-template"
  service_docker_image_tag  = "localbuild"
  runtime_enable            = "false"
  net_service_type          = "LoadBalancer"
  service_image_pull_policy = "IfNotPresent"
  service_cmd               = "/home/app/run.sh"

  service_container_port = "80"

  service_resource_requests_memory = "128Mi"
  service_resource_limits_memory = "128Mi"

  service_readiness_probe_path = "/health"

  service_environments_variables = <<EOF
    ENVIRONMENT=local
   EOF
}
