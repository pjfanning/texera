package edu.uci.ics.amber.engine.architecture.worker

object WorkerInternalQueue{


}




abstract class WorkerInternalQueue {

  def enqueueCommand():Unit

  def enqueueData():Unit

  def peek():Option[]

  def take()

}
