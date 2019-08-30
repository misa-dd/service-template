@Library('common-pipelines@10.15.0') _

import groovy.transform.Field

/**
 * Expected inputs:
 * ----------------
 * params['SHA']                - SHA containing the migrations that should be run
 * params['GITHUB_REPOSITORY']  - GitHub ssh url of repository (git://....)
 * params['JSON']               - Extensible json doc with extra information
 */

@Field
def canDeployToProd = false

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
    stage('Migrate staging') {
      steps {
        artifactoryLogin()
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          common.migrateService(params['GITHUB_REPOSITORY'], params['SHA'], 'staging')
        }
      }
    }
    stage('Continue to prod?') {
      steps {
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          canDeployToProd = common.inputCanDeployToProd("Migrate production?")
        }
      }
    }
    stage('Migrate prod') {
      when {
        equals expected: true, actual: canDeployToProd
      }
      steps {
        artifactoryLogin()
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          common.migrateService(params['GITHUB_REPOSITORY'], params['SHA'], 'prod')
        }
      }
      post {
        success {
          script {
            tag = getImmutableReleaseSemverTag(params['SHA'])
          }
          sendSlackMessage 'eng-deploy-manifest', "Successful migrate of ${common.getServiceName()} to ${tag}: <${BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>"
        }
        failure {
          script {
            tag = getImmutableReleaseSemverTag(params['SHA'])
          }
          sendSlackMessage 'eng-deploy-manifest', "Migrate failed for ${common.getServiceName()} to ${tag}: <${BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>"
        }
      }
    }
  }
}
