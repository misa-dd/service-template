github = new org.doordash.Github()
os = new org.doordash.Os()

def buildImages(gitUrl, sha, serviceId) {
  stage('Build') {
    buildSlave {
      github.doClosureWithStatus(
        {
          os.deleteDirContentsAsRoot()
          github.fastCheckoutScm(gitUrl, sha, serviceId)
          withCredentials([string(credentialsId: 'PIP_EXTRA_INDEX_URL', variable: 'PIP_EXTRA_INDEX_URL')]) {
            sh """|#!/bin/bash
                  |set -x
                  |cd ${serviceId}
                  |PIP_EXTRA_INDEX_URL=${PIP_EXTRA_INDEX_URL} make dockerbuildtagpush
                  |""".stripMargin()
          }
        },
        gitUrl,
        sha,
        "Docker Images",
        "${BUILD_URL}console"
      )
    }
  }
}

def runTests(gitUrl, sha, serviceId) {
  stage('Testing') {
    genericSlave {
      github.doClosureWithStatus(
        {
          os.deleteDirContentsAsRoot()
          github.fastCheckoutScm(gitUrl, sha, serviceId)
          sh """|#!/bin/bash
                |cd ${serviceId}
                |make test
                |""".stripMargin()
        },
        gitUrl,
        sha,
        "Testing",
        "${BUILD_URL}console"
      )
    }
  }
}

return this
