@Library('common-pipelines@v10.0.127') _

import groovy.transform.Field
import org.doordash.JenkinsDd

/**
 * Expected inputs:
 * ----------------
 * params['TAG']                - Tag used to start the pipeline
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
