package edu.uci.ics.amber.engine.recovery

abstract class SecondaryLogStorage {

  // for persist

  def persistCurrentDataCursor(dataCursor: Long)

  // for recovery:

  def load(): Iterable[Long]

  // clear everything

  def clear():Unit

}
