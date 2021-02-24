package edu.uci.ics.amber.engine.common

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.recovery.empty.{EmptyMainLogStorage, EmptySecondaryLogStorage}

import scala.concurrent.duration._

object Constants {
  val defaultBatchSize = 400
  val remoteHDFSPath = "hdfs://10.138.0.2:8020"
  val remoteHDFSIP = "10.138.0.2"
  var defaultNumWorkers = 0
  var dataset = 0
  var masterNodeAddr: String = null

  var numWorkerPerNode = 2
  var dataVolumePerNode = 10
  var defaultTau: FiniteDuration = 10.milliseconds

}
