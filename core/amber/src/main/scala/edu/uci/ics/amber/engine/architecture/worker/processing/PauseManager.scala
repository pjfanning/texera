package edu.uci.ics.amber.engine.architecture.worker.processing

import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue
import edu.uci.ics.amber.engine.common.ambermessage.ChannelEndpointID
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class PauseManager extends Serializable {

  private val globalPauses = new mutable.HashSet[PauseType]()
  private val specificInputPauses =
    new mutable.HashMap[PauseType, mutable.Set[ActorVirtualIdentity]]
      with mutable.MultiMap[PauseType, ActorVirtualIdentity]

  @transient
  private var internalQueue: WorkerInternalQueue = _

  def initialize(internalQueue: WorkerInternalQueue): Unit = {
    this.internalQueue = internalQueue
  }

  def pause(pauseType: PauseType): Unit = {
    globalPauses.add(pauseType)
    // disable all data queues
    internalQueue.enableAllDataQueue(false)
  }

  def pauseInputChannel(pauseType: PauseType, inputs: List[ActorVirtualIdentity]): Unit = {
    inputs.foreach(input => {
      specificInputPauses.addBinding(pauseType, input)
      // disable specified data queues
      internalQueue.enableDataQueue(ChannelEndpointID(input, false), false)
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
      internalQueue.enableAllDataQueue(true)
      return
    }
    // need to resume specific input channels
    val pausedChannels = specificInputPauses.values.flatten.toSet
    internalQueue.enableAllDataQueue(true)
    pausedChannels.foreach{
      input =>
        internalQueue.enableDataQueue(ChannelEndpointID(input, false), false)
    }
  }

  def isPaused(): Boolean = {
    globalPauses.nonEmpty
  }

}
