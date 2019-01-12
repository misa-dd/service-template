@Library('common-pipelines@v9.1.26') _

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

common = new org.doordash.utils.experimental.Common()
gitUrl = params['GITHUB_REPOSITORY']
sha = params['SHA']

stage('GitHub Status') {
  curlSlave {
    common.setGitHubShaStatus(gitUrl, sha, message: 'Start Jenkinsfile-nodeploy Pipeline')
  }
}

stage('Build') {
  buildSlave {
    common.dockerBuildTagPush(gitUrl, sha, branch: params['BRANCH_NAME'])
  }
}

stage('Testing') {
  genericSlave {
    common.runCommand(gitUrl, sha, command: 'echo "test placeholder"')
  }
}
