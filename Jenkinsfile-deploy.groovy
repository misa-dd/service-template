@Library('common-pipelines@v6.0.15')
import java.time.Instant

import org.doordash.Docker
import org.doordash.Github
import org.doordash.Os
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
        os = new Os()
        slack = new Slack()
        github.sendStatusToGitHub(
            params["SHA"],
            params["GITHUB_REPOSITORY"],
            "Started.",
            "Start Jenkinsfile-deploy Pipeline",
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

stage('Deploy') {
    try {
        timeout(time: 8, unit: 'MINUTES') {
            def input = input(message: "select a fabric",
                parameters: [
                    choice(name: 'target', choices: 'custom\nstaging\nprod', description: 'What fabric to deploy to?'),
                    [name: 'targetCustom', $class: 'TextParameterDefinition', description: 'specify custom fabric']
                ]
            )
            if (input['target'] == "custom") {
                targetFabric = input['targetCustom']
                targetCluster = 'sandbox'
            } else {
                targetFabric = input['target']
                targetCluster = targetFabric
            }
        }
    } catch(err) {
        error('Aborted due to timeout!')
    }
    // TODO (bliang) simplify
    genericSlave {
        os.deleteContextDirSubDirsWithExceptions("$WORKSPACE", ["doordash-containertools"])
        gitUrl = params["GITHUB_REPOSITORY"].toString()
        sha = params["SHA"].toString()
        isBranch = (params["BRANCH_NAME"] != "master") ? ' --is-branch y' : ''
        serviceId = github.extractGitUrlParts(gitUrl)[1]
        serviceDir = "$WORKSPACE/$serviceId"
        github.fastCheckoutScm(gitUrl, sha, serviceDir)
        awsAccountId = sh(script: "aws sts get-caller-identity --output text --query Account", returnStdout: true).trim()

        credentialsId = 'K8S_CONFIG_' + targetCluster.toUpperCase()
        // not pip3 since only used for render.py on Jenkins slaves
        withCredentials([file(credentialsId: credentialsId, variable: credentialsId)]) {
            sh """
            mkdir -p $WORKSPACE/.kube
            cp \$$credentialsId $WORKSPACE/.kube/config.$targetCluster
            \$(aws ecr get-login --no-include-email --region us-west-2)
            docker run -e KUBECONFIG=/root/.kube/config.$targetCluster -v $WORKSPACE/.kube:/root/.kube -v $serviceDir:/root/$serviceId 611706558220.dkr.ecr.us-west-2.amazonaws.com/doordash/deployment-tools.app:latest bash -c "cd /root/$serviceId && python3 render.py --src infra/k8s --dst .tmp --fabric $targetFabric$isBranch --aws-account-id $awsAccountId && kubectl apply -f /root/$serviceId/.tmp/app.yaml -n $targetFabric && timeout 150 kubectl rollout status deployment/$serviceId -n $targetFabric --watch"
            """
        }
    }
}
