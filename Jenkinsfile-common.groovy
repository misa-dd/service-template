def getServiceName() {
  return 'service-template'
}


def envToPromoteArgs(String environment) {

  def promoteArgs = [
    k8sCluster: environment,
    k8sNamespace: getServiceName()
  ]

  // handle custom environments
  parsedEnvironment = environment.toString().split(":")
  if (parsedEnvironment.size() == 2) {  
    domain = parsedEnvironment[1]
    promoteArgs.k8sNamespace = "${promoteArgs.k8sNamespace}-${domain}"
    promoteArgs.k8sCluster = parsedEnvironment[0]
  }

  // reject in case of errors
  if (parsedEnvironment.size() > 2 || !(promoteArgs.k8sCluster in ['staging', 'prod'])) {
    error("Invalid value for environment: ${environment} - Aborting pipeline.")
  }

  println """About to return from envToPromoteArgs:
     - k8sCluster: ${promoteArgs.k8sCluster}, 
     - k8sNamespace: ${promoteArgs.k8sNamespace}
  """

  return promoteArgs
}

return this