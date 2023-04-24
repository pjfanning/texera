package edu.uci.ics.amber.engine.architecture.checkpoint

import akka.serialization.Serialization
import edu.uci.ics.amber.engine.architecture.worker.ReplayCheckpointConfig
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

class PlannedCheckpoint(val conf:ReplayCheckpointConfig) extends SavedCheckpoint {

  var completionCount:Int = conf.recordInputAt.size + 1

  def decreaseCompletionCount(): Unit ={
    completionCount -= 1
  }

}
