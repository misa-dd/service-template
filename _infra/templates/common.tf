variable "k8s_config_path" {
  type = string
}

variable "image_tag" {
  type = string
}

variable "namespace" {
  type = string
}

variable "service_name" {
  type = string
}

provider "aws" {
  region  = "us-west-2"
  version = "~> 2.19"
}

provider "kubernetes" {
  config_path = var.k8s_config_path
}

provider "helm" {
  kubernetes {
    config_path = var.k8s_config_path
  }
  install_tiller = false
  version = "~> 0.10"
}
