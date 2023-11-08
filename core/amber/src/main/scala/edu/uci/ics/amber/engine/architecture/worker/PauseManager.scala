package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkInputGateway
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.ChannelID
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class PauseManager(val actorId: ActorVirtualIdentity, inputPort: NetworkInputGateway)
    extends AmberLogging {

  private val globalPauses = new mutable.HashSet[PauseType]()
  private val specificInputPauses =
    new mutable.HashMap[PauseType, mutable.Set[ActorVirtualIdentity]]
      with mutable.MultiMap[PauseType, ActorVirtualIdentity]

  def pause(pauseType: PauseType): Unit = {
    globalPauses.add(pauseType)
    // disable all data queues
    inputPort.getAllDataChannels.foreach(_.enable(false))
  }

  def pauseInputChannel(pauseType: PauseType, inputs: List[ActorVirtualIdentity]): Unit = {
    inputs.foreach(input => {
      specificInputPauses.addBinding(pauseType, input)
      // disable specified data queues
      inputPort.getChannel(ChannelID(actorId, input, isControlChannel = false)).enable(false)
    })
  }

  def resume(pauseType: PauseType): Unit = {
    globalPauses.remove(pauseType)
    specificInputPauses.remove(pauseType)

    // still globally paused no action, don't need to resume anything
    if (globalPauses.nonEmpty) {
      return
    }
    // global pause is empty, specific input pause is also empty, resume all
    if (specificInputPauses.isEmpty) {
      inputPort.getAllDataChannels.foreach(_.enable(true))
      return
    }
    // need to resume specific input channels
    val pausedActorVids = specificInputPauses.values.flatten.toSet
    inputPort.getAllDataChannels.foreach(_.enable(true))
    pausedActorVids.foreach { vid =>
      inputPort.getChannel(ChannelID(actorId, vid, isControlChannel = false)).enable(false)
    }
  }

  def isPaused: Boolean = {
    globalPauses.nonEmpty
  }

}
