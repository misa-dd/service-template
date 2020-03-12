/**
 * Expected inputs:
 * ----------------
 * params['SHA']                - SHA containing the migrations that should be run
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
    stage('Migrate env') {
      steps {
        script {
          common = load "${WORKSPACE}/Jenkinsfile-common.groovy"
          common.migrateService(GIT_URL, params['SHA'], params['ENV'])
        }
      }
    }
  }
  post {
    success {
      sendSlackMessage common.getSlackChannel(), "Successful migrate of ${common.getServiceName()}:${params['REF']} to ${params['ENV']}: <${BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>"
    }
    failure {
      sendSlackMessage common.getSlackChannel(), "Migrate failed for ${common.getServiceName()}:${params['REF']} to ${params['ENV']}: <${BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>"
    }
  }
}
