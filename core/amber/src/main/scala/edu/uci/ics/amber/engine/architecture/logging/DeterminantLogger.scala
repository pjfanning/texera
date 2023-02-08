package edu.uci.ics.amber.engine.architecture.logging

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

abstract class DeterminantLogger extends Serializable {

  def setCurrentSender(sender:ActorVirtualIdentity): Unit

  def stepIncrement(): Unit

  def logDeterminant(inMemDeterminant: InMemDeterminant): Unit

  def drainCurrentLogRecords(): Array[InMemDeterminant]

}
