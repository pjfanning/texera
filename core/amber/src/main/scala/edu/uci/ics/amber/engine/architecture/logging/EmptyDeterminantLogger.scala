package edu.uci.ics.amber.engine.architecture.logging
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

class EmptyDeterminantLogger extends DeterminantLogger {

  override def logDeterminant(inMemDeterminant: InMemDeterminant): Unit = {}

  override def drainCurrentLogRecords(): Array[InMemDeterminant] = { Array.empty }

  override def stepIncrement(): Unit = {}

  override def setCurrentSender(sender: ActorVirtualIdentity): Unit = {}
}
