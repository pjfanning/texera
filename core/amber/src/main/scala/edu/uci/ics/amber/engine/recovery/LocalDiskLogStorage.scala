package edu.uci.ics.amber.engine.recovery

class LocalDiskLogStorage[T](logName: String) extends LogStorage[T](logName) {

  override def load(): Iterable[T] = ???

  override def persistElement(elem: T): Unit = ???

  override def clear(): Unit = ???
}
