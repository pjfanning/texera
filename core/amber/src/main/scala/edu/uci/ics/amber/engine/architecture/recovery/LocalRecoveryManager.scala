package edu.uci.ics.amber.engine.architecture.recovery

import scala.collection.mutable.ArrayBuffer

class LocalRecoveryManager {

  private val callbacksOnStart = new ArrayBuffer[() => Unit]()
  private val callbacksOnEnd = new ArrayBuffer[() => Unit]()
  var notifyReplayStatus: () => Unit = () => {}

  def registerOnStart(callback: () => Unit): Unit = {
    callbacksOnStart.append(callback)
  }

  def registerOnEnd(callback: () => Unit): Unit = {
    callbacksOnEnd.append(callback)
  }

  def setNotifyReplayCallback(callback: () => Unit): Unit = {
    notifyReplayStatus = callback
  }

  def Start(): Unit = {
    callbacksOnStart.foreach(callback => callback())
  }

  def End(): Unit = {
    callbacksOnEnd.foreach(callback => callback())
  }

}
