@Library('common-pipelines@v10.0.22') _

def deployHelm(Map optArgs = [:], String gitUrl, String sha) {
    optArgs = [targetCluster: 'default', targetNamespace: 'default', targetConfig: '*', doorctlVersion: 'v0.0.104'] << optArgs
    serviceName = 'service-template'

    build(job: "microservice/microservice-helm-deploy", parameters: [
      [$class: 'StringParameterValue', name: 'CLUSTER', value: optArgs.targetCluster],
      [$class: 'StringParameterValue', name: 'NAMESPACE', value: optArgs.targetNamespace],
      [$class: 'StringParameterValue', name: 'SERVICE_NAME', value: serviceName],
      [$class: 'StringParameterValue', name: 'SHA', value: sha],
    ])
}

return this
