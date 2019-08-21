@Library('common-pipelines@10.15.0') _

/**
 * Expected inputs:
 * ----------------
 * params['SHA']                - Sha to build
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
          // In the event you'd like the semver tag, here's how to retrieve it.
          // Please don't use the tag value to pull code from github because
          // github tags are mutable. Only use the SHA to deal with git.
          env.tag = getImmutableReleaseSemverTag(params['SHA'])
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          common.dockerBuild(params['GITHUB_REPOSITORY'], tag: params['TAG'])
        }
      }
    }
  }
}
