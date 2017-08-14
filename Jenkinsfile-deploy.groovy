@Library('common-pipelines@v3.0.11')
import java.time.Instant

import org.doordash.Github
import org.doordash.Os
import org.doordash.Workspace

def NODE_TYPE = "general || spot"

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

stage('Deploy') {
    def input = input(message: "select an environment",
        parameters: [
            choice(name: 'target', choices: 'custom\nstaging\nprod', description: 'What environment to deploy to?'),
            [name: 'targetCustom', $class: 'TextParameterDefinition', description: 'specify custom environment']
        ]
    )
    if (input['target'] == "custom") {
        targetEnv = input['targetCustom']
    } else {
        targetEnv = input['target']
    }
    // TODO (bliang) simplify
    node(NODE_TYPE) {
        g = new Github()
        o = new Os()
        w = new Workspace()
        sh 'mkdir -p /root/.kube'
        o.deleteContextDirSubDirsWithExceptions("${WORKSPACE}", ["doordash-containertools"])
        git_url = params["GITHUB_REPOSITORY"].toString()
        sha = params["SHA"].toString()
        serviceid = g.extractGitUrlParts(git_url)[1]
        service_dir = "${WORKSPACE}/${serviceid}"
        g.fastCheckoutScm(git_url, sha, service_dir)

        if (targetEnv == 'prod') {
            withCredentials([file(credentialsId: 'K8S_CONFIG_PROD', variable: 'K8S_CONFIG_PROD')]) {
                sh """
                cp "$K8S_CONFIG_PROD" /root/.kube/config.prod
                cd $serviceid
                pip install -r requirements.txt
                python render.py infra/k8s .tmp prod
                \$(aws ecr get-login --no-include-email --region us-west-2)
                docker run -e KUBECONFIG=/root/.kube/config.prod -v /root/.kube:/root/.kube -v $service_dir:/root/$serviceid 611706558220.dkr.ecr.us-west-2.amazonaws.com/doordash/deployment-tools.app:latest kubectl apply -f /root/$serviceid/.tmp/app.yaml
                """
            }
        } else if (targetEnv == 'staging') {
            withCredentials([file(credentialsId: 'K8S_CONFIG_STAGING', variable: 'K8S_CONFIG_STAGING')]) {
                sh """
                cp "$K8S_CONFIG_STAGING" /root/.kube/config.staging
                cd $serviceid
                pip install -r requirements.txt
                python render.py infra/k8s .tmp staging
                \$(aws ecr get-login --no-include-email --region us-west-2)
                docker run -e KUBECONFIG=/root/.kube/config.staging -v /root/.kube:/root/.kube -v $service_dir:/root/$serviceid 611706558220.dkr.ecr.us-west-2.amazonaws.com/doordash/deployment-tools.app:latest kubectl apply -f /root/$serviceid/.tmp/app.yaml
                """
            }
        } else {
            withCredentials([file(credentialsId: 'K8S_CONFIG_SANDBOX', variable: 'K8S_CONFIG_SANDBOX')]) {
                sh """
                cp "$K8S_CONFIG_SANDBOX" /root/.kube/config.sandbox
                cd $serviceid
                pip install -r requirements.txt
                python render.py infra/k8s .tmp $targetEnv
                \$(aws ecr get-login --no-include-email --region us-west-2)
                docker run -e KUBECONFIG=/root/.kube/config.sandbox -v /root/.kube:/root/.kube -v $service_dir:/root/$serviceid 611706558220.dkr.ecr.us-west-2.amazonaws.com/doordash/deployment-tools.app:latest kubectl apply -f /root/$serviceid/.tmp/app.yaml
                """
            }
        }
    }
}

