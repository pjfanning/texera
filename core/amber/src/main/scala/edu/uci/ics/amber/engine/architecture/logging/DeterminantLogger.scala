package edu.uci.ics.amber.engine.architecture.logging

abstract class DeterminantLogger extends Serializable {

  def stepIncrement():Unit

  def logDeterminant(inMemDeterminant: InMemDeterminant): Unit

  def drainCurrentLogRecords(): Array[InMemDeterminant]

}
