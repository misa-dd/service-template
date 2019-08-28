@Library('common-pipelines@10.15.0') _

import groovy.transform.Field
import org.doordash.JenkinsDd

/**
 * Expected inputs:
 * ----------------
 * params['SHA']                - Sha being requested for promotion
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
    stage('Deploy to staging') {
      steps {
        artifactoryLogin()
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          common.deployService(params['GITHUB_REPOSITORY'], params['SHA'], 'staging')
        }
      }
    }
    stage('Deploy Pulse to staging') {
      steps {
        artifactoryLogin()
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          common.deployPulse(params['GITHUB_REPOSITORY'], params['SHA'], 'staging')
        }
      }
    }
    stage('Continue to prod?') {
      steps {
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          canDeployToProd = common.inputCanDeployToProd()
        }
      }
    }
    stage('Deploy to prod') {
      when {
        equals expected: true, actual: canDeployToProd
      }
      steps {
        artifactoryLogin()
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          common.deployService(params['GITHUB_REPOSITORY'], params['SHA'], 'prod')
          tag = getImmutableReleaseSemverTag(params['SHA'])
        }
        sendSlackMessage 'eng-deploy-manifest', "Successful promote of ${common.getServiceName()} to ${tag}: <${JenkinsDd.instance.getBlueOceanJobUrl()}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>"
      }
    }
    stage('Deploy Pulse to prod') {
      when {
        equals expected: true, actual: canDeployToProd
      }
      steps {
        artifactoryLogin()
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          common.deployPulse(params['GITHUB_REPOSITORY'], params['SHA'], 'prod')
        }
      }
    }
  }
}
