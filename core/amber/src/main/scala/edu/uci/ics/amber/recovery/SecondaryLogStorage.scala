package edu.uci.ics.amber.recovery

abstract class SecondaryLogStorage {

  // for persist

  def persistCurrentDataCursor(dataCursor: Long)

  //for recovery:

  def load(): Iterable[Long]

}
