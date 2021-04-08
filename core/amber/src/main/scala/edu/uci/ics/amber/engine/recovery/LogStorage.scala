package edu.uci.ics.amber.engine.recovery

abstract class LogStorage[T] extends Serializable {

  // for persist:
  def persistElement(elem: T)

  // for recovery:
  def load(): Iterable[T]

  // delete everything
  def clear(): Unit

  // release the resources
  def release(): Unit

}
