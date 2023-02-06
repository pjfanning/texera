package edu.uci.ics.amber.engine.architecture.execution

class LinkExecution(totalReceiversCount: Int) {
  private var currentCompletedCount = 0

  def incrementCompletedReceiversCount(): Unit = currentCompletedCount += 1

  def isCompleted: Boolean = currentCompletedCount == totalReceiversCount
}
