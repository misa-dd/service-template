@Library('common-pipelines@v10.0.127') _

import groovy.transform.Field
import org.doordash.JenkinsDd

// -----------------------------------------------------------------------------------
// The following params are automatically provided by the callback gateway as inputs
// to the Jenkins pipeline that starts this job.
//
// params["SHA"]                    - Sha used to start the pipeline
// params["BRANCH_NAME"]            - Name of GitHub branch the SHA is associated with
// params["UNIQUE_BUILD_ID"]        - A randomly generated unique ID for this job run
// params["ENQUEUED_AT_TIMESTAMP"]  - Unix timestamp generated by callback gateway
// params["JSON"]                   - Extensible json doc with extra information
// params["GITHUB_REPOSITORY"]      - GitHub ssh url of repository (git://....)
// -----------------------------------------------------------------------------------

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
    stage('Docker Build') {
      steps {
        artifactoryLogin()
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          common.dockerBuild(params['GITHUB_REPOSITORY'], params['SHA'], params['BRANCH_NAME'], common.getServiceName())
        }
      }
    }
    stage('Deploy to staging') {
      steps {
        artifactoryLogin()
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          common.deployService(params['GITHUB_REPOSITORY'], params['SHA'], params['BRANCH_NAME'], common.getServiceName(), 'staging')
        }
      }
    }
    stage('Deploy Pulse to staging') {
      steps {
        artifactoryLogin()
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          common.deployPulse(params['GITHUB_REPOSITORY'], params['SHA'], params['BRANCH_NAME'], common.getServiceName(), 'staging')
        }
      }
    }
    stage('Continue to prod?') {
      when {
        branch 'master'
      }
      steps {
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          canDeployToProd = common.inputCanDeployToProd()
        }
      }
    }
    stage('Deploy to prod') {
      when {
        branch 'master'
        equals expected: true, actual: canDeployToProd
      }
      steps {
        artifactoryLogin()
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          common.deployHelm(params['GITHUB_REPOSITORY'], params['SHA'], params['BRANCH_NAME'], common.getServiceName(), 'prod')
        }
        sendSlackMessage 'eng-deploy-manifest', "Successfully deployed ${common.getServiceName()}: <${JenkinsDd.instance.getBlueOceanJobUrl()}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>"
      }
    }
    stage('Deploy Pulse to prod') {
      when {
        branch 'master'
        equals expected: true, actual: canDeployToProd
      }
      steps {
        artifactoryLogin()
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          common.deployPulse(params['GITHUB_REPOSITORY'], params['SHA'], params['BRANCH_NAME'], common.getServiceName(), 'prod')
        }
      }
    }
  }
}
