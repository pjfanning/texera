package edu.uci.ics.amber.engine.recovery

import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort.WorkflowControlMessage
import edu.uci.ics.amber.engine.common.virtualidentity.VirtualIdentity
import edu.uci.ics.amber.engine.recovery.MainLogStorage.MainLogElement

object MainLogStorage {
  trait MainLogElement
  case class DataMessageIdentifier(sender: VirtualIdentity, seq: Long) extends MainLogElement
}

abstract class MainLogStorage {

  //for persist:

  def persistentEntireMessage(message: WorkflowControlMessage)

  def persistSenderIdentifier(sender: VirtualIdentity, seq: Long)

  //for recovery:

  def load(): Iterable[MainLogElement]

}
