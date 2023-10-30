package edu.uci.ics.amber.engine.architecture.controller

class LinkExecution(totalReceiversCount: Int) extends Serializable {
  private var currentCompletedCount = 0

  def incrementCompletedReceiversCount(): Unit = currentCompletedCount += 1

  def isCompleted: Boolean = currentCompletedCount == totalReceiversCount
}
