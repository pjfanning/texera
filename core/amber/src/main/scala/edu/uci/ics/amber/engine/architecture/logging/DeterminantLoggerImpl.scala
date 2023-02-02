package edu.uci.ics.amber.engine.architecture.logging

import scala.collection.mutable

class DeterminantLoggerImpl extends DeterminantLogger {

  private val tempLogs = mutable.ArrayBuffer[InMemDeterminant]()
  private var last = 0L
  private var cur = 0L

  def updateStep(currentStep:Long):Unit = {
    cur = currentStep
  }

  def logDeterminant(inMemDeterminant: InMemDeterminant, currentStep: Long): Unit = {
    pushStepDelta(currentStep - last)
    cur = currentStep
    last = currentStep
    tempLogs.append(inMemDeterminant)
  }

  def drainCurrentLogRecords(): Array[InMemDeterminant] = {
    pushStepDelta(cur - last)
    val result = tempLogs.toArray
    tempLogs.clear()
    result
  }

  private def pushStepDelta(delta: Long): Unit = {
    if (delta <= 1L) {
      return
    }
    tempLogs.append(StepDelta(delta - 1))
  }

}
