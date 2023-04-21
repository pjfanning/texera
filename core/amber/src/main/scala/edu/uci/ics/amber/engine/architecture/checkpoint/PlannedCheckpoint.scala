package edu.uci.ics.amber.engine.architecture.checkpoint

import akka.serialization.Serialization
import edu.uci.ics.amber.engine.architecture.worker.ReplayCheckpointConfig
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

class PlannedCheckpoint(actorId:ActorVirtualIdentity, val conf: ReplayCheckpointConfig, serialization: Serialization) {

  private var completionCount = conf.recordInputAt.size + 1
  val chkpt = new SavedCheckpoint()
  chkpt.attachSerialization(serialization)

  def decreaseCompletionCount(): Unit ={
    completionCount -= 1
    if(completionCount == 0){
      CheckpointHolder.addCheckpoint(actorId, conf.checkpointAt, chkpt)
    }
  }

}
