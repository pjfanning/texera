package edu.uci.ics.amber.engine.architecture.logging

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class DeterminantLoggerImpl extends DeterminantLogger {

  private val tempLogs = mutable.ArrayBuffer[InMemDeterminant]()
  private var step = 0L
  private var currentSender:ActorVirtualIdentity = _

  override def setCurrentSender(sender: ActorVirtualIdentity): Unit = {
    if(currentSender != sender){
      pushStepDelta()
    }
    currentSender = sender
  }

  def stepIncrement(): Unit = {
    step += 1
  }

  def logDeterminant(inMemDeterminant: InMemDeterminant): Unit = {
    pushStepDelta()
    tempLogs.append(inMemDeterminant)
  }

  def drainCurrentLogRecords(): Array[InMemDeterminant] = {
    pushStepDelta()
    val result = tempLogs.toArray
    tempLogs.clear()
    result
  }

  private def pushStepDelta(): Unit = {
    if (step <= 0L) {
      return
    }
    tempLogs.append(StepDelta(currentSender, step))
    step = 0L
  }

}
