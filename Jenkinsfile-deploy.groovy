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
    stage('Deploy To Staging?') {
      steps {
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          canDeployPipeline = common.inputDeployPipeline()
        }
      }
    }
    stage('Staging Deployment') {
      when {
        equals expected: true, actual: canDeployPipeline
      }

      stages {
        stage('Deploy to Staging') {
          steps {
            script {
              common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
              deployService(params['GITHUB_REPOSITORY'], params['SHA'], 'staging', common.getServiceName())
            }
          }
        }
        stage('Deploy Blocking Pulse to Staging') {
          steps {
            script {
              common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
              common.deployBlockingPulse(params['GITHUB_REPOSITORY'], params['SHA'], 'staging')
            }
          }
        }
      }

      post {
        success {
          sendSlackMessage common.getSlackChannel(), "Successful deploy to staging of ${common.getServiceName()} using sha ${params['SHA']} of branch ${params['BRANCH_NAME']}: <${BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>"
        }
        failure {
          sendSlackMessage common.getSlackChannel(), "Deploy to staging failed for ${common.getServiceName()} using sha ${params['SHA']} of branch ${params['BRANCH_NAME']}: <${BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>"
        }
      }

    }
  }
}
