package edu.uci.ics.amber.engine.architecture.recovery

trait CheckpointRestoreManager {

  def loadFromCheckpoint()

  def setupReplay()

  def setupCheckpointDuringReplay()

  def startProcessing()


}
