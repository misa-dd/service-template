/**
 * Expected inputs:
 * ----------------
 * params['SHA']                - Sha to promote
 * GIT_URL                      - GitHub https url of repository (https://github.com/....)
 * params['JSON']               - Extensible json doc with extra information
 * params['REF']                - The reference (tag, branch, or sha) from the ddops command line
 * params['ENV']                - The environment from the ddops command line
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
    stage('Deploy to env') {
      steps {
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          deployService(GIT_URL, params['SHA'], params['ENV'], common.getServiceName())
        }
      }
    }
    stage('Deploy Blocking Pulse to env') {
      steps {
        node('docker-build') {
          script {
            gitClone(repo: GIT_URL, ref: params['SHA'])
            common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
            deployBlockingPulse(GIT_URL, params['SHA'], params['ENV'], common.getServiceName(), pulseVersion: '2.1')
          }
        }
      }
    }
  }
  post {
    success {
      sendSlackMessage common.getSlackChannel(), "Successful promote of ${common.getServiceName()}:${params['REF']} to ${params['ENV']}: <${BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>"
    }
    failure {
      sendSlackMessage common.getSlackChannel(), "Promote failed for ${common.getServiceName()}:${params['REF']} to ${params['ENV']}: <${BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>"
    }
  }
}
