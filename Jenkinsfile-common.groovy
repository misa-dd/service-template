doorctl = new org.doordash.Doorctl()
github = new org.doordash.Github()

DOORCTL_VERSION="v0.0.61"

def buildImages(gitUrl, sha, serviceId) {
  stage('Build') {
    buildSlave {
      try {
        github.doClosureWithStatus(
          {
            deleteDir()
            def doorCtlPath = doorctl.installIntoWorkspace(DOORCTL_VERSION)
            github.fastCheckoutScm(gitUrl, sha, serviceId)
            sh """|#!/bin/bash
                  |set -x
                  |cd $serviceId
                  |make build tag push branch=${params["BRANCH_NAME"]} doorctl=${doorCtlPath}
                  |""".stripMargin()
          },
          gitUrl,
          sha,
          "Build",
          "${BUILD_URL}console"
        )
      } catch (e) {
        currentBuild.result = "FAILED"
        throw e
      }
    }
  }
}

def runTests(gitUrl, sha, serviceId) {
  stage('Testing') {
    genericSlave {
      try {
        github.doClosureWithStatus({
          deleteDir()
          def doorCtlPath = doorctl.installIntoWorkspace(DOORCTL_VERSION)
          github.fastCheckoutScm(gitUrl, sha, serviceId)
          sh """|#!/bin/bash
                |set -x
                |cd $serviceId
                |make test
                |""".stripMargin()
        },
        gitUrl,
        sha,
        "Testing",
        "${BUILD_URL}console")
      } catch (e) {
        currentBuild.result = "FAILED"
        throw e
      }
    }
  }
}

return this
