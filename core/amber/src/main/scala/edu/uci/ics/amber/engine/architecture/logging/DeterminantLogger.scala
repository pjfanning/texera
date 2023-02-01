package edu.uci.ics.amber.engine.architecture.logging

abstract class DeterminantLogger {

  def stepIncrement(): Unit

  def stepDecrement(): Unit

  def logDeterminant(inMemDeterminant: InMemDeterminant): Unit

  def drainCurrentLogRecords(): Array[InMemDeterminant]

}
