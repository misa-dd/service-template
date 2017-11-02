@Library('common-pipelines@v9.0.8')
import java.time.Instant

import org.doordash.Docker
import org.doordash.Github
import org.doordash.Slack

// -----------------------------------------------------------------------------------
// ********* WARNING *********
//
// On the CI server, Jenkins will *PERSIST* these input params across all branches.
// That means even though this file may be something you are editing on a branch,
// the jenkins Pipelining on the Jenkins master server will attach these input params
// to the main pipeline runner and enforce them across all branches. Do not change
// any of this unless you are absolutely sure you know what you are doing.
// -----------------------------------------------------------------------------------
properties(
    [
        parameters(
            [
                string(name: 'SHA',                     description: 'Sha to build.'),
                string(name: 'BRANCH_NAME',             description: 'Name of branch being built. Example: "myFeature".'),
                string(name: 'UNIQUE_BUILD_ID',         description: 'A unique id used to identify this build.'),
                string(name: 'ENQUEUED_AT_TIMESTAMP',   description: 'Unix timestamp of when the job was enqueued.'),
                string(name: 'JSON',                    description: 'A JSON document with extended information.', defaultValue: ''),
                string(name: 'GITHUB_REPOSITORY',       description: 'The github repository the commit originates from.', defaultValue: "git@github.com:doordash/doordash-bar.git")
            ]
        )
    ]
)

stage('Startup'){
    curlSlave {
        docker = new Docker()
        github = new Github()
        slack = new Slack()
        github.sendStatusToGitHub(
            params["SHA"],
            params["GITHUB_REPOSITORY"],
            "Started.",
            "Start Jenkinsfile-nodeploy Pipeline",
            "${BUILD_URL}console"
        )
    }
}

stage('Build'){
    buildSlave {
        github.doClosureWithStatus(
            {
                docker.buildPushContainers(
                    params["GITHUB_REPOSITORY"].toString(),
                    params["BRANCH_NAME"].toString(),
                    params["SHA"].toString()
                )
            },
            params["GITHUB_REPOSITORY"],
            params["SHA"],
            "Docker Images",
            "${BUILD_URL}console"
        )
    }
}

stage('Testing'){
    genericSlave {
        github.doClosureWithStatus(
            {
                docker.runMakeTargetOnService(
                    params["GITHUB_REPOSITORY"].toString(),
                    params["BRANCH_NAME"].toString(),
                    params["SHA"].toString(),
                    "make test"
                )
            },
            params["GITHUB_REPOSITORY"],
            params["SHA"],
            "Testing",
            "${BUILD_URL}console"
        )
    }
}
