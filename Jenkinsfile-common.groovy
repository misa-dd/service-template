import java.util.regex.Pattern

import org.doordash.Docker
import org.doordash.Doorctl
import org.doordash.Github
import org.doordash.Pulse

/**
 * Returns the service name which is useful for builds and deployments.
 */
def getServiceName() {
  return 'service-template'
}

/**
 * Installs terraform into the _infra directory when it doesn't exist.
 */
def installTerraform() {
  sh """|#!/bin/bash
        |set -ex
        |
        |# Install Terraform
        |pushd _infra
        |rm -rf terraform
        |wget -q -nc https://releases.hashicorp.com/terraform/0.12.3/terraform_0.12.3_linux_amd64.zip
        |unzip terraform_0.12.3_linux_amd64.zip
        |chmod +x terraform
        |popd
        |""".stripMargin()
}

/**
 * Build, Tag, and Push a Docker image for a Microservice.
 * If there already exists a docker image for the sha, then it will skip 'make docker-build tag push'.
 * <br>
 * <br>
 * Requires:
 * <ul>
 * <li>Makefile with docker-build, tag, push, and remove-docker-images targets
 * </ul>
 * Provides the docker-build target in Makefile with:
 * <ul>
 * <li>CACHE_FROM = url:tag of recent Docker image to speed up subsequent builds that use the --cache-from option
 * <li>PIP_EXTRA_INDEX_URL = pip extra index URL for installing Python packages
 * </ul>
 */
def dockerBuild(Map optArgs = [:], String gitUrl, String sha) {
  String gitRepo = getGitRepoName(gitUrl)
  Map o = [
    dockerImageUrl: "611706558220.dkr.ecr.us-west-2.amazonaws.com/${gitRepo}",
    setArtifactoryEnvs: false
  ] << optArgs

  // Ensure we have a SHA
  if (sha == null) {
    error("Git SHA is required.")
  }

  // Try to pull an already pushed docker image from ECR
  String loadedCacheDockerTag
  try {
    sh """|#!/bin/bash
          |set -ex
          |docker pull ${o.dockerImageUrl}:${sha}
          |""".stripMargin()
    println "Docker image was found for ${o.dockerImageUrl}:${sha} - Skipping 'make docker-build tag push'"
    loadedCacheDockerTag = sha
  } catch (oops) {
    println "No docker image was found for ${o.dockerImageUrl}:${sha} - Running 'make docker-build tag push'"
  }

  def credentials = [string(credentialsId: 'PIP_EXTRA_INDEX_URL', variable: 'PIP_EXTRA_INDEX_URL')]

  if (o.setArtifactoryEnvs) {
    credentials += [
      string(credentialsId: 'ARTIFACTORY_MACHINE_USER_NAME', variable: 'ARTIFACTORY_USERNAME'),
      string(credentialsId: 'ARTIFACTORY_MACHINE_USER_PASS', variable: 'ARTIFACTORY_PASSWORD')
    ]
  }

  // Build, tag, and push docker image when it isn't in ECR
  if (loadedCacheDockerTag == null) {
    loadedCacheDockerTag = new Docker().findAvailableCacheFrom(gitUrl, sha, o.dockerImageUrl)
    if (loadedCacheDockerTag == null) {
      loadedCacheDockerTag = "noCacheFoundxxxxxxx"
    }
    String cacheFromValue = "${o.dockerImageUrl}:${loadedCacheDockerTag}"

    // Use Terraform to create the ECR Repo when it doesn't exist
    installTerraform()
    sshagent (credentials: ['DDGHMACHINEUSER_PRIVATE_KEY']) { // Required for terraform to git clone
      sh """|#!/bin/bash
            |set -ex
            |pushd _infra/build
            |rm -rf .terraform terraform.*
            |sed 's/_GITREPO_/${gitRepo}/g' ecr.tf.template > ecr.tf
            |terraform="${WORKSPACE}/_infra/terraform"
            |\${terraform} init
            |\${terraform} plan -out terraform.tfplan -var="service_name=${getServiceName()}"
            |\${terraform} apply terraform.tfplan
            |popd
            |""".stripMargin()
    }

    // Build, tag, and push the sha to ECR
    withCredentials(credentials) {
      sh """|#!/bin/bash
            |set -ex
            |make docker-build tag push \\
            | CACHE_FROM=${cacheFromValue} \\
            | PIP_EXTRA_INDEX_URL=${PIP_EXTRA_INDEX_URL}
            |""".stripMargin()
    }
  }

  // If semver is associated with sha, tag and push the semver to ECR
  String tag = null
  try {
    tag = getImmutableReleaseSemverTag(sha)
  } catch (err) {
    println "Sha does not have an associated semver tag. Skipping docker semver tag push."
  }
  if (tag != null) {
    sh """|#!/bin/bash
          |set -ex
          |docker tag ${o.dockerImageUrl}:${sha} ${o.dockerImageUrl}:${tag}
          |docker push ${o.dockerImageUrl}:${tag}
          |# Add latest tag for security scans of our latest docker images
          |docker tag ${o.dockerImageUrl}:${sha} ${o.dockerImageUrl}:latest
          |docker push ${o.dockerImageUrl}:latest
          |""".stripMargin()
  }

  // Cleanup
  sh """|#!/bin/bash
        |set -ex
        |make remove-docker-images
        |""".stripMargin()
}

/**
 * Migrate a Microservice.
 */
def migrateService(Map optArgs = [:], String gitUrl, String sha, String env) {
  Map o = [
    k8sCredFileCredentialId: "K8S_CONFIG_${env.toUpperCase()}_NEW",
    k8sCluster: env,
    k8sNamespace: gitUrl,
  ] << envToOptArgs(gitUrl, env) << optArgs

  String tag = getImmutableReleaseSemverTag(sha)

  // For example, use a Makefile target to migrate
  withCredentials([file(credentialsId: o.k8sCredFileCredentialId, variable: 'k8sCredsFile')]) { // Required for k8s config
    sh """|#!/bin/bash
          |set -ex
          |make migrate \\
          | tag=${tag} \\
          | env=${env} \\
          | k8sNamespace=${o.k8sNamespace} \\
          | k8sCluster=${o.k8sCluster}
          |""".stripMargin()
  }
}

/**
 * Deploy a Microservice.
 */
def deployService(Map optArgs = [:], String gitUrl, String sha, String env) {
  Map o = [
    k8sCredFileCredentialId: "K8S_CONFIG_${env.toUpperCase()}_NEW",
    k8sCluster: env,
    k8sNamespace: gitUrl,
  ] << envToOptArgs(gitUrl, env) << optArgs
  String tag = getImmutableReleaseSemverTag(sha)
  installTerraform()
  sshagent (credentials: ['DDGHMACHINEUSER_PRIVATE_KEY']) { // Required for terraform to git clone
    withCredentials([file(credentialsId: o.k8sCredFileCredentialId, variable: 'k8sCredsFile')]) { // Required for k8s config
      sh """|#!/bin/bash
            |set -ex
            |
            |# Use Terraform to create the namespace when it doesn't exist
            |pushd _infra/namespace/${o.k8sCluster}
            |rm -rf .terraform terraform.*
            |sed 's/_GITREPO_/${o.k8sNamespace}/g' namespace.tf.template > namespace.tf
            |terraform="${WORKSPACE}/_infra/terraform"
            |\${terraform} init
            |\${terraform} plan -out terraform.tfplan \\
            | -var="k8s_config_path=${k8sCredsFile}" \\
            | -var="namespace=${o.k8sNamespace}" \\
            | -var="service_account_namespace=${o.k8sCluster}"
            |\${terraform} apply terraform.tfplan
            |popd
            |
            |# Use Terraform to deploy the service
            |pushd _infra/${o.k8sCluster}
            |rm -rf .terraform terraform.*
            |sed 's/_GITREPO_/${o.k8sNamespace}/g' service.tf.template > service.tf
            |cp -f ${WORKSPACE}/_infra/templates/common.tf common.tf
            |terraform="${WORKSPACE}/_infra/terraform"
            |\${terraform} init
            |\${terraform} plan -out terraform.tfplan \\
            | -var="k8s_config_path=${k8sCredsFile}" \\
            | -var="image_tag=${tag}" \\
            | -var="namespace=${o.k8sNamespace}" \\
            | -var="service_name=${getServiceName()}"
            |\${terraform} apply terraform.tfplan
            |popd
            |""".stripMargin()
    }
  }
}

/**
 * Deploy Pulse for a Microservice.
 */
def deployPulse(Map optArgs = [:], String gitUrl, String sha, String env) {
  Map o = [
    k8sCluster: env,
    k8sNamespace: gitUrl,
    pulseVersion: '2.0',
    pulseDoorctlVersion: 'v0.0.119',
    pulseRootDir: 'pulse'
  ] << envToOptArgs(gitUrl, env) << optArgs

  String PULSE_VERSION = o.pulseVersion
  String SERVICE_NAME = getServiceName()
  String KUBERNETES_CLUSTER = o.k8sCluster
  String KUBERNETES_NAMESPACE = o.k8sCluster // Use o.k8sNamespace once Pulse can be deployed to the service namespace
  String DOORCTL_VERSION = o.pulseDoorctlVersion
  String PULSE_DIR = o.pulseRootDir

  sshagent(credentials: ['DDGHMACHINEUSER_PRIVATE_KEY']) {
    // install doorctl and grab its executable path
    String doorctlPath = new Doorctl().installIntoWorkspace(DOORCTL_VERSION)
    // deploy Pulse
    new Pulse().deploy(PULSE_VERSION, SERVICE_NAME, KUBERNETES_CLUSTER, doorctlPath, PULSE_DIR, KUBERNETES_NAMESPACE, null, sha)
  }
}

/**
 * Return the name of the repo taken from the end of the Git URL.
 * Throw an assertion error if the Git Repo name is not valid for use as a kubernetes namespace.
 * It must be less than 64 alphanumeric characters and may contain dashes.
 */
def getGitRepoName(String gitUrl) {
  String gitRepo = gitUrl.tokenize('/').last().split("\\.git")[0]
  assert gitRepo.length() < 64
  assert gitRepo ==~ /^[a-z0-9]([-a-z0-9]*[a-z0-9])?$/ :
      "The Git Repo name is not valid for use as a kubernetes namespace. " +
      "It must be less than 64 alphanumeric characters and may contain dashes"
  return gitRepo
}

/**
 * Given an environment name like 'sandbox1', 'staging', and 'production',
 * resolve the optional arguments that vary per environment.
 */
def envToOptArgs(String gitUrl, String env) {
  String gitRepo = getGitRepoName(gitUrl)
  if (env ==~ /^sandbox([0-9]|1[0-5])/) { // sandbox0 - sandbox15
    return [
      k8sCredFileCredentialId: 'K8S_CONFIG_STAGING_NEW',
      k8sCluster: 'staging',
      k8sNamespace: gitRepo,
    ]
  } else if (env == 'staging') {
    return [
      k8sCredFileCredentialId: 'K8S_CONFIG_STAGING_NEW',
      k8sCluster: 'staging',
      k8sNamespace: gitRepo,
    ]
  } else if (env == 'prod' || env == 'production') {
    return [
      k8sCredFileCredentialId: 'K8S_CONFIG_PROD_NEW',
      k8sCluster: 'prod',
      k8sNamespace: gitRepo,
    ]
  } else {
    error("Unknown env value of '${env}' passed.")
  }
}

/**
 * Prompt the user to decide if we can deploy to production.
 * The user has 10 minutes to choose between Proceed or Abort.
 * If Proceed, then we should proceed. If Abort or Timed-out,
 * then we should cleanly skip the rest of the steps in the
 * pipeline without failing the pipeline.
 *
 * @return True if we can deploy to prod. False, otherwise.
 */
def inputCanDeployToProd(String message = 'Deploy to production') {
  boolean canDeployToProd = false
  try {
    timeout(time: 10, unit: 'MINUTES') {
      input(id: 'userInput', message: message)
      canDeployToProd = true
    }
  } catch (err) {
    println "Timed out or Aborted! Will not deploy to production."
    println err
  }
  return canDeployToProd
}

return this
