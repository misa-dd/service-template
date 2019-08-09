@Library('common-pipelines@v10.0.127') _

import org.doordash.JenkinsDd

/**
 * Expected inputs:
 * ----------------
 * params['TAG']                - Tag used to start the pipeline
 * params['GITHUB_REPOSITORY']  - GitHub ssh url of repository (git://....)
 * params['JSON']               - Extensible json doc with extra information
 * params['REASON']             - Reason why hotfix is being used
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
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          common.deployService(params['GITHUB_REPOSITORY'], params['TAG'], 'prod')
        }
        sendSlackMessage 'eng-deploy-manifest', "Successful hotfix of ${common.getServiceName()} to ${params['TAG']}: <${JenkinsDd.instance.getBlueOceanJobUrl()}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>"
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
