import com.doordash.Deploy

/**
 * Returns the service name which is useful for builds and deployments.
 */
def getServiceName() {
  return 'service-template'
}

/**
 * Returns the service slack channel which is useful for notifying of builds and deployments.
 */
def getSlackChannel() {
  return 'pulse-svc-template'
}

/**
 * Migrate a Microservice.
 */
def migrateService(Map optArgs = [:], String gitUrl, String sha, String env) {
  def deploy = new Deploy()
  Map o = [
    k8sCredFileCredentialId: "K8S_CONFIG_${env.toUpperCase()}_NEW",
    k8sCluster: env,
    k8sNamespace: gitUrl,
  ] << deploy.envToOptArgs(gitUrl, env) << optArgs

  String tag = sha

  try {
    tag = getImmutableReleaseSemverTag(sha)
  } catch (err) {
    println "Sha does not have an associated semver tag. Using SHA as tag."
  }

  // For example, use a Makefile target to migrate
  withCredentials([file(credentialsId: o.k8sCredFileCredentialId, variable: 'k8sCredsFile')]) { // Required for k8s config
    sh """|#!/bin/bash
          |set -ex
          |echo "Migrated ${getServiceName()} to ${tag} for ${env} within ${o.k8sNamespace} on ${o.k8sCluster}"
          |""".stripMargin()
  }
}

return this
