package edu.uci.ics.amber.engine.architecture.worker

case class StateRestoreConfig(fromCheckpoint:Option[Long], replayTo:Option[Long])
