package edu.uci.ics.amber.engine.architecture.logging

abstract class DeterminantLogger {

  def updateStep(currentStep:Long):Unit

  def logDeterminant(inMemDeterminant: InMemDeterminant, currentStep: Long): Unit

  def drainCurrentLogRecords(): Array[InMemDeterminant]

}
