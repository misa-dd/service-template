@Library('common-pipelines@v10.0.90') _

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
    stage('Startup') {
      steps {
        setGitHubStatus 'Start Jenkinsfile-nodeploy Pipeline', 'Started'
        artifactoryLogin()
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
        }
      }
    }
    stage('Docker Build') {
      steps {
        script {
          reportClosureAsGitHubStatus {
            common.dockerBuild(params['GITHUB_REPOSITORY'], params['SHA'], params['BRANCH_NAME'], common.getServiceName())
          }
        }
      }
    }
  }
}
