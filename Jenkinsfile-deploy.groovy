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
    skipStagesAfterUnstable()
  }
  agent {
    label 'universal'
  }
  stages {
    stage('Startup') {
      steps {
        artifactoryLogin()
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          gitUrl = params['GITHUB_REPOSITORY']
          sha = params['SHA']
          branch = params['BRANCH_NAME']
          serviceName = common.getServiceName()
        }
      }
    }
    stage('Docker Build') {
      steps {
        script {
          common.dockerBuild(gitUrl, sha, branch, serviceName)
        }
      }
    }
    stage('Deploy to staging') {
      steps {
        script {
          common.deployHelm(gitUrl, sha, branch, serviceName, 'staging')
        }
      }
    }
    stage('Deploy Pulse to staging') {
      steps {
        script {
          common.deployPulse(gitUrl, sha, branch, serviceName, 'staging')
        }
      }
    }
    stage('Continue to prod?') {
      steps {
        timeout(time: 10, unit: 'MINUTES') {
          input 'Deploy to production?'
        }
      }
    }
    stage('Deploy to prod') {
      steps {
        script {
          common.deployHelm(gitUrl, sha, branch, serviceName, 'prod')
        }
      }
    }
    stage('Deploy Pulse to prod') {
      steps {
        script {
          common.deployPulse(gitUrl, sha, branch, serviceName, 'prod')
        }
      }
    }
  }
}
