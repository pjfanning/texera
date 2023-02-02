package edu.uci.ics.amber.engine.architecture.logging

class EmptyDeterminantLogger extends DeterminantLogger {

  override def updateStep(currentStep:Long):Unit = {}

  override def logDeterminant(inMemDeterminant: InMemDeterminant, currentStep:Long): Unit = {}

  override def drainCurrentLogRecords(): Array[InMemDeterminant] = { Array.empty }

}
