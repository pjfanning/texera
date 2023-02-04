package edu.uci.ics.amber.engine.architecture.logging

class EmptyDeterminantLogger extends DeterminantLogger {

  override def logDeterminant(inMemDeterminant: InMemDeterminant): Unit = {}

  override def drainCurrentLogRecords(): Array[InMemDeterminant] = { Array.empty }

  override def stepIncrement(): Unit = {}
}
