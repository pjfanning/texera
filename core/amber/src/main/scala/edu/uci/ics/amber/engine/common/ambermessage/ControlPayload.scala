package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipReply}

sealed trait ControlPayload extends WorkflowDPMessagePayload with WorkflowExecutionPayload

object ControlInvocation {
  def apply(controlCommand: ControlCommand[_] with SkipReply): ControlInvocation = {
    ControlInvocation(-1, controlCommand)
  }
}

case class ControlInvocation(commandID: Long, command: ControlCommand[_]) extends ControlPayload

case class ReturnInvocation(originalCommandID: Long, controlReturn: Any) extends ControlPayload