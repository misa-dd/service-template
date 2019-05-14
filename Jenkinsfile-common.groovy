@Library('common-pipelines@v10.0.90') _

docker = new org.doordash.Docker()
doorctl = new org.doordash.Doorctl()
github = new org.doordash.Github()
pulse = new org.doordash.Pulse()

def getServiceName() {
  return 'service-template'
}

def dockerBuild(Map optArgs = [:], String gitUrl, String sha, String branch, String serviceName) {
  Map o = [
    dockerDoorctlVersion: 'v0.0.104',
    dockerImageUrl: "ddartifacts-docker.jfrog.io/doordash/${serviceName}"
  ] << optArgs
  String doorctlPath
  sshagent (credentials: ['DDGHMACHINEUSER_PRIVATE_KEY']) {
    doorctlPath = doorctl.installIntoWorkspace(o.dockerDoorctlVersion)
  }
  String loadedCacheDockerTag = docker.findAvailableCacheFrom(gitUrl, sha, o.dockerImageUrl)
  if (loadedCacheDockerTag == null) {
    loadedCacheDockerTag = "noCacheFoundxxxxxxx"
  }
  String cacheFromValue = "${o.dockerImageUrl}:${loadedCacheDockerTag}"
  shWithCredentials({
      sh """|#!/bin/bash
            |set -ex
            |make docker-build tag push \\
            | branch=${branch}
            | doorctl=${doorctlPath} \\
            | SHA=${sha} \\
            | CACHE_FROM=${cacheFromValue} \\
            | PIP_EXTRA_INDEX_URL=${PIP_EXTRA_INDEX_URL}
            |""".stripMargin()
    },
    ['PIP_EXTRA_INDEX_URL']
  )
}

def deployHelm(Map optArgs = [:], String gitUrl, String sha, String branch, String serviceName, String env) {
  Map o = [
    helmCommand: 'upgrade',
    helmFlags: '--install',
    helmChartPath: "_infra/charts/${serviceName}",
    helmValuesFile: "values-${env}.yaml",
    helmRelease: ${serviceName},
    k8sCredFileCredentialId: 'K8S_CONFIG_${env.toUpperCase()}_NEW',
    k8sNamespace: ${env},
    tillerNamespace: ${env},
    timeoutSeconds: 600
  ] << serviceNameEnvToOptArgs(serviceName, env) << optArgs
  withCredentials([file(credentialsId: ${o.k8sCredFileCredentialId}, variable: 'k8sCredsFile')]) {
    sh """|#!/bin/bash
          |set -ex
          |
          |alias helm="docker run --rm -v ${k8sCredsFile}:/root/.kube/config -v ${WORKSPACE}:/apps alpine/helm:2.10.0"
          |HELM_OPTIONS="${o.helmCommand} ${o.helmRelease} ${o.helmChartPath} \\
          | --values ${o.helmChartPath}/${o.helmValuesFile} --set image.tag=${sha} ${o.helmFlags} \\
          | --tiller-namespace ${o.tillerNamespace} --namespace ${o.k8sNamespace} \\
          | --wait --timeout ${o.timeoutSeconds}"
          |
          |# log manifest to CI/CD
          |helm \${HELM_OPTIONS} --debug --dry-run
          |
          |helm \${HELM_OPTIONS}
          |""".stripMargin()
  }
}

def deployPulse(Map optArgs = [:], String gitUrl, String sha, String branch, String serviceName, String env) {
  Map o = [
    k8sNamespace: ${env},
    pulseVersion: '2.1',
    pulseDoorctlVersion: 'v0.0.113',
    pulseRootDir: 'pulse'
  ] << serviceNameEnvToOptArgs(serviceName, env) << optArgs

  String PULSE_VERSION = o.pulseVersion
  String SERVICE_NAME = serviceName
  String KUBERNETES_CLUSTER = o.k8sNamespace
  String DOORCTL_VERSION = o.pulseDoorctlVersion
  String PULSE_ROOT_DIR = o.pulseRootDir
  String PULSE_DIR = SERVICE_NAME+"/"+PULSE_ROOT_DIR

  sshagent(credentials: ['DDGHMACHINEUSER_PRIVATE_KEY']) {
    // checkout the repo
    github.fastCheckoutScm(gitUrl, sha, serviceName)
    // install doorctl and grab its executable path
    String doorctlPath = doorctl.installIntoWorkspace(DOORCTL_VERSION)
    // deploy Pulse
    pulse.deploy(PULSE_VERSION, SERVICE_NAME, KUBERNETES_CLUSTER, doorctlPath, PULSE_DIR)
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
      helmRelease: ${serviceName},
      k8sCredFileCredentialId: 'K8S_CONFIG_STAGING_NEW',
      k8sNamespace: 'staging',
      tillerNamespace: 'staging'
    ]
  } else if (env == 'prod' || env == 'production') {
    return [
      helmFlags: '--install',
      helmValuesFile: 'values-prod.yaml',
      helmRelease: ${serviceName},
      k8sCredFileCredentialId: 'K8S_CONFIG_PROD_NEW',
      k8sNamespace: 'prod',
      tillerNamespace: 'prod'
    ]
  } else {
    error("Unknown env value of '${env}' passed.")
  }
}

return this
