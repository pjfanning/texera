package edu.uci.ics.amber.engine.architecture.scheduling.resourcePolicies

import edu.uci.ics.amber.engine.architecture.scheduling.Region

class ExecutionClusterInfo() {

  def getAvailableNumOfWorkers(region: Region): Int = {
    val numberOfProcessors = Runtime.getRuntime.availableProcessors()
    numberOfProcessors * 2
  }
}
