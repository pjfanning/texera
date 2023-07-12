package edu.uci.ics.amber.engine.architecture.logging

import edu.uci.ics.amber.engine.architecture.logging.ChannelStepCursor.INIT_STEP
import edu.uci.ics.amber.engine.common.ambermessage.ChannelEndpointID

object ChannelStepCursor {
  // step value before processing any incoming message
  // processing first message will have step = 0
  val INIT_STEP: Long = -1L
}

class ChannelStepCursor {
  private var currentStepCounter: Long = INIT_STEP
  private var currentChannel: ChannelEndpointID = _

  def setCurrentChannel(channel: ChannelEndpointID): Unit = {
    currentChannel = channel
  }

  def getStep: Long = currentStepCounter

  def getChannel: ChannelEndpointID = currentChannel

  def stepIncrement(): Unit = {
    currentStepCounter += 1
  }

}
