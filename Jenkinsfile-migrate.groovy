@Library('common-pipelines@10.15.0') _

import groovy.transform.Field
import org.doordash.JenkinsDd

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
          // In the event you'd like the semver tag, here's how to retrieve it.
          // Please don't use the tag value to pull code from github because
          // github tags are mutable. Only use the SHA to deal with git.
          env.tag = getImmutableReleaseSemverTag(params['SHA'])
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          common.migrateService(params['GITHUB_REPOSITORY'], params['TAG'], 'staging')
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
          common.migrateService(params['GITHUB_REPOSITORY'], params['TAG'], 'prod')
        }
        sendSlackMessage 'eng-deploy-manifest', "Successful migrate of ${common.getServiceName()} to ${params['TAG']}: <${JenkinsDd.instance.getBlueOceanJobUrl()}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>"
      }
    }
  }
}
