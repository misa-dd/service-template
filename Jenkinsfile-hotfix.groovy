/**
 * Expected inputs:
 * ----------------
 * params['SHA']                - Sha to hotfix to
 * GIT_URL                      - GitHub https url of repository (https://github.com/....)
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
    kubernetes {
      label podLabel('universal')
      yaml """
apiVersion: v1
kind: Pod
metadata:
  annotations:
    iam.amazonaws.com/role: ${getKube2IamRole()}
spec:
  containers:
  - name: release-default-buster
    image: 839591177169.dkr.ecr.us-west-2.amazonaws.com/default-debian-buster-cicd-worker:latest
    command: ["cat"]
    tty: true
"""
    }
  }
  stages {
    stage('Deploy to prod') {
      steps {
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          deployService(GIT_URL, params['SHA'], 'prod', common.getServiceName())
        }
      }
    }
    stage('Deploy Pulse to prod') {
      steps {
        node('docker-build') {
          script {
            gitClone(repo: GIT_URL, ref: params['SHA'])
            common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
            deployPulse(GIT_URL, params['SHA'], 'prod', common.getServiceName(), pulseVersion: '2.1')
          }
        }
      }
    }
  }
  post {
    success {
      script {
        tag = getImmutableReleaseSemverTag(params['SHA'])
      }
      sendSlackMessage common.getSlackChannel(), "Successful hotfix of ${common.getServiceName()} to ${tag}: <${BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>"
    }
    failure {
      script {
        tag = getImmutableReleaseSemverTag(params['SHA'])
      }
      sendSlackMessage common.getSlackChannel(), "Hotfix failed for ${common.getServiceName()} to ${tag}: <${BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>"
    }
  }
}
