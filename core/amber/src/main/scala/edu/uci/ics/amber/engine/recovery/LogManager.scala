package edu.uci.ics.amber.engine.recovery

abstract class LogManager(logStorage: LogStorage[_]) extends RecoveryComponent {

  def releaseLogStorage(): Unit ={
    logStorage.release()
  }
}
