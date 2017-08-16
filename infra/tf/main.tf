terraform {
  backend "s3" {
    bucket = "dd-service-tf-state-staging"
    key    = "{{fabric}}/{{service}}/terraform.tfstate"
    region = "us-west-2"
  }
}

provider "aws" {
  region = "us-west-2"
}
