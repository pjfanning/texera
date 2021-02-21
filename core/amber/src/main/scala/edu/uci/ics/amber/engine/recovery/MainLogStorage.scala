package edu.uci.ics.amber.engine.recovery

import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort.WorkflowControlMessage
import edu.uci.ics.amber.engine.common.virtualidentity.VirtualIdentity
import edu.uci.ics.amber.engine.recovery.MainLogStorage.{FromID, IdentifierMapping, MainLogElement}

import scala.collection.mutable

object MainLogStorage {
  trait MainLogElement
  case class IdentifierMapping(virtualId: VirtualIdentity, id: Int) extends MainLogElement
  case class FromID(id: Int) extends MainLogElement
}

abstract class MainLogStorage {

  private val mapping = mutable.HashMap[VirtualIdentity, Int]()
  private var counter = 0

  //for persist:
  def persistElement(elem: MainLogElement)

  def receivedDataFrom(vid: VirtualIdentity): Unit = {
    if (!mapping.contains(vid)) {
      mapping(vid) = counter
      persistElement(IdentifierMapping(vid, counter))
      counter += 1
    }
    persistElement(FromID(mapping(vid)))
  }

  //for recovery:

  def load(): Iterable[MainLogElement]

}
