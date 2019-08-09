@Library('common-pipelines@v10.0.127') _

/**
 * Expected inputs:
 * ----------------
 * params['TAG']                - Tag used to start the pipeline
 * params['GITHUB_REPOSITORY']  - GitHub ssh url of repository (git://....)
 * params['JSON']               - Extensible json doc with extra information
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
    stage('Docker Build') {
      steps {
        artifactoryLogin()
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          common.dockerBuild(params['GITHUB_REPOSITORY'], tag: params['TAG'])
        }
      }
    }
  }
}
