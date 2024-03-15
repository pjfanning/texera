package edu.uci.ics.amber.engine.architecture.scheduling.resourcePolicies

import edu.uci.ics.amber.engine.architecture.scheduling.Region

class ExecutionClusterInfo() {
  private var numOfRegion: Int = 1

  def setNumOfRegions(numOfRegion: Int): Unit = {
    this.numOfRegion = numOfRegion
  }

  def getNumOfRegions: Int = {
     this.numOfRegion
  }
  def getAvailableNumOfWorkers(region: Region): Int = {
    val numberOfProcessors = Runtime.getRuntime.availableProcessors()
    numberOfProcessors * 2 / numOfRegion
  }
}
