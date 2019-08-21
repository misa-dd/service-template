@Library('common-pipelines@10.15.0') _

import org.doordash.JenkinsDd

/**
 * Expected inputs:
 * ----------------
 * params['SHA']                - Sha to rollback to
 * params['GITHUB_REPOSITORY']  - GitHub ssh url of repository (git://....)
 * params['JSON']               - Extensible json doc with extra information
 * params['REASON']             - Reason why rollback is being used
 */

pipeline {
  options {
    timestamps()
    skipStagesAfterUnstable()
    timeout(time: 30, unit: 'MINUTES')
  }
  agent {
    label 'universal'
  }
  stages {
    stage('Deploy to prod') {
      steps {
        artifactoryLogin()
        script {
          // In the event you'd like the semver tag, here's how to retrieve it.
          // Please don't use the tag value to pull code from github because
          // github tags are mutable. Only use the SHA to deal with git.
          env.tag = getImmutableReleaseSemverTag(params['SHA'])
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          common.deployService(params['GITHUB_REPOSITORY'], params['TAG'], 'prod')
        }
        sendSlackMessage 'eng-deploy-manifest', "Successful rollback of ${common.getServiceName()} to ${params['TAG']}: <${JenkinsDd.instance.getBlueOceanJobUrl()}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>"
      }
    }
    stage('Deploy Pulse to prod') {
      steps {
        artifactoryLogin()
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          common.deployPulse(params['GITHUB_REPOSITORY'], params['TAG'], 'prod')
        }
      }
    }
  }
}
