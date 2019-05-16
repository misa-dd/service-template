@Library('common-pipelines@v10.0.90') _

import org.doordash.Docker
import org.doordash.Doorctl
import org.doordash.Pulse

/**
 * Returns the service name which is useful for builds and deployments.
 */
def getServiceName() {
  return 'service-template'
}

/**
 * Build, Tag, and Push a Docker image for a Microservice.
 * If there already exists a docker image for the sha, then it will skip 'make docker-build tag push'.
 * <br>
 * <br>
 * Requires:
 * <ul>
 * <li>Makefile with docker-build, tag, and push targets
 * </ul>
 * Provides:
 * <ul>
 * <li>branch = GitHub branch name
 * <li>doorctl = Path in order to execute doorctl from within the Makefile
 * <li>SHA = GitHub SHA
 * <li>CACHE_FROM = url:tag of recent Docker image to speed up subsequent builds that use the --cache-from option
 * <li>PIP_EXTRA_INDEX_URL = pip extra index URL for installing Python packages
 * </ul>
 */
def dockerBuild(Map optArgs = [:], String gitUrl, String sha, String branch, String serviceName) {
  Map o = [
    dockerDoorctlVersion: 'v0.0.118',
    dockerImageUrl: "ddartifacts-docker.jfrog.io/doordash/${serviceName}"
  ] << optArgs
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
  if (loadedCacheDockerTag == null) {
    loadedCacheDockerTag = new Docker().findAvailableCacheFrom(gitUrl, sha, o.dockerImageUrl)
    if (loadedCacheDockerTag == null) {
      loadedCacheDockerTag = "noCacheFoundxxxxxxx"
    }
    String doorctlPath
    sshagent (credentials: ['DDGHMACHINEUSER_PRIVATE_KEY']) {
      doorctlPath = new Doorctl().installIntoWorkspace(o.dockerDoorctlVersion)
    }
    String cacheFromValue = "${o.dockerImageUrl}:${loadedCacheDockerTag}"
    shWithCredentials({
        sh """|#!/bin/bash
              |set -ex
              |make docker-build tag push \\
              | branch=${branch} \\
              | doorctl=${doorctlPath} \\
              | SHA=${sha} \\
              | CACHE_FROM=${cacheFromValue} \\
              | PIP_EXTRA_INDEX_URL=${PIP_EXTRA_INDEX_URL}
              |""".stripMargin()
      },
      ['PIP_EXTRA_INDEX_URL']
    )
  }
}

/**
 * Deploy a Microservice using Helm.
 */
def deployHelm(Map optArgs = [:], String gitUrl, String sha, String branch, String serviceName, String env) {
  Map o = [
    helmCommand: 'upgrade',
    helmFlags: '--install',
    helmChartPath: "_infra/charts/${serviceName}",
    helmValuesFile: "values-${env}.yaml",
    helmRelease: serviceName,
    k8sCredFileCredentialId: "K8S_CONFIG_${env.toUpperCase()}_NEW",
    k8sNamespace: env,
    tillerNamespace: env,
    timeoutSeconds: 600
  ] << serviceNameEnvToOptArgs(serviceName, env) << optArgs
  withCredentials([file(credentialsId: o.k8sCredFileCredentialId, variable: 'k8sCredsFile')]) {
    sh """|#!/bin/bash
          |set -ex
          |
          |helm="docker run --rm -v ${k8sCredsFile}:/root/.kube/config -v ${WORKSPACE}:/apps alpine/helm:2.10.0"
          |HELM_OPTIONS="${o.helmCommand} ${o.helmRelease} ${o.helmChartPath} \\
          | --values ${o.helmChartPath}/${o.helmValuesFile} --set image.tag=${sha} ${o.helmFlags} \\
          | --tiller-namespace ${o.tillerNamespace} --namespace ${o.k8sNamespace} \\
          | --wait --timeout ${o.timeoutSeconds}"
          |
          |# log manifest to CI/CD
          |\${helm} \${HELM_OPTIONS} --debug --dry-run
          |
          |\${helm} \${HELM_OPTIONS}
          |""".stripMargin()
  }
}

/**
 * Deploy Pulse for a Microservice.
 */
def deployPulse(Map optArgs = [:], String gitUrl, String sha, String branch, String serviceName, String env) {
  Map o = [
    k8sNamespace: env,
    pulseVersion: '2.1',
    pulseDoorctlVersion: 'v0.0.118',
    pulseRootDir: 'pulse'
  ] << serviceNameEnvToOptArgs(serviceName, env) << optArgs

  String PULSE_VERSION = o.pulseVersion
  String SERVICE_NAME = serviceName
  String KUBERNETES_CLUSTER = o.k8sNamespace
  String DOORCTL_VERSION = o.pulseDoorctlVersion
  String PULSE_DIR = o.pulseRootDir

  sshagent(credentials: ['DDGHMACHINEUSER_PRIVATE_KEY']) {
    // install doorctl and grab its executable path
    String doorctlPath = new Doorctl().installIntoWorkspace(DOORCTL_VERSION)
    // deploy Pulse
    new Pulse().deploy(PULSE_VERSION, SERVICE_NAME, KUBERNETES_CLUSTER, doorctlPath, PULSE_DIR)
  }
}

/**
 * Given a service name and environment name like 'sandbox1', 'staging', and 'production',
 * resolve the optional arguments that vary per environment.
 */
def serviceNameEnvToOptArgs(String serviceName, String env) {
  if (env ==~ /^sandbox([0-9]|1[0-5])/) { // sandbox0 - sandbox15
    return [
      helmFlags: '--install --force',
      helmValuesFile: "values-${env}.yaml",
      helmRelease: "${serviceName}-${env}",
      k8sCredFileCredentialId: 'K8S_CONFIG_STAGING_NEW',
      k8sNamespace: 'staging',
      tillerNamespace: 'staging'
    ]
  } else if (env == 'staging') {
    return [
      helmFlags: '--install --force',
      helmValuesFile: 'values-staging.yaml',
      helmRelease: serviceName,
      k8sCredFileCredentialId: 'K8S_CONFIG_STAGING_NEW',
      k8sNamespace: 'staging',
      tillerNamespace: 'staging'
    ]
  } else if (env == 'prod' || env == 'production') {
    return [
      helmFlags: '--install',
      helmValuesFile: 'values-prod.yaml',
      helmRelease: serviceName,
      k8sCredFileCredentialId: 'K8S_CONFIG_PROD_NEW',
      k8sNamespace: 'prod',
      tillerNamespace: 'prod'
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
def inputCanDeployToProd() {
  boolean canDeployToProd = false
  try {
    timeout(time: 10, unit: 'MINUTES') {
      input(id: 'userInput', message: 'Deploy to production?')
      canDeployToProd = true
    }
  } catch (err) {
    println "Timed out or Aborted! Will not deploy to production."
    println err
  }
  return canDeployToProd
}

return this
