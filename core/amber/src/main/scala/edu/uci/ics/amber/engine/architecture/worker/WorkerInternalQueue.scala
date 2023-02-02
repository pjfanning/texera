//package edu.uci.ics.amber.engine.architecture.worker
//
//import edu.uci.ics.amber.engine.architecture.logging.{DeterminantLogger, LogManager}
//import edu.uci.ics.amber.engine.architecture.recovery.{LocalRecoveryManager, ReplayGate}
//import edu.uci.ics.amber.engine.architecture.worker.DataProcessor.{ControlElement, DataElement}
//import edu.uci.ics.amber.engine.common.Constants
//import edu.uci.ics.amber.engine.common.ambermessage.{ControlPayload, DataFrame, EndOfUpstream}
//import edu.uci.ics.amber.engine.common.tuple.ITuple
//import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LinkIdentity}
//import lbmq.LinkedBlockingMultiQueue
//
//import java.util.concurrent.locks.ReentrantLock
//import scala.collection.mutable
//
//object WorkerInternalQueue {
//}
//
///** Inspired by the mailbox-ed thread, the internal queue should
//  * be a part of DP thread.
//  */
//trait WorkerInternalQueue {
//
//  private val lock = new ReentrantLock()
//
//  val dataQueue = new ProactiveDeque[DataElement]
//
//  val controlQueue = new ProactiveDeque[ControlElement]
//
//  def pauseManager: PauseManager
//  // logging related variables:
//  def logManager: LogManager // require dp thread to have log manager
//  def recoveryQueue: ReplayGate // require dp thread to have recovery queue
//  lazy val determinantLogger: DeterminantLogger = logManager.getDeterminantLogger
//  var totalSteps = 0L
//
//  // the values in below maps are in tuples (not batches)
//  private var inputTuplesPutInQueue =
//    new mutable.HashMap[ActorVirtualIdentity, Long]() // read and written by main thread
//  @volatile private var inputTuplesTakenOutOfQueue =
//    new mutable.HashMap[ActorVirtualIdentity, Long]() // written by DP thread, read by main thread
//
//  def getSenderCredits(sender: ActorVirtualIdentity): Int = {
//    (Constants.unprocessedBatchesCreditLimitPerSender * Constants.defaultBatchSize - (inputTuplesPutInQueue
//      .getOrElseUpdate(sender, 0L) - inputTuplesTakenOutOfQueue.getOrElseUpdate(
//      sender,
//      0L
//    )).toInt) / Constants.defaultBatchSize
//  }
//
//  def appendElement(elem: DataElement): Unit = {
//    if (Constants.flowControlEnabled) {
//      elem match {
//        case InputTuple(from, _) =>
//          inputTuplesPutInQueue(from) = inputTuplesPutInQueue.getOrElseUpdate(from, 0L) + 1
//        case _ =>
//        // do nothing
//      }
//    }
//    if (recoveryQueue.isReplayCompleted) {
//      // may have race condition with restoreInput which happens inside DP thread.
//      lock.lock()
//      dataQueue.enqueue(elem)
//      lock.unlock()
//    } else {
//      recoveryQueue.addData(elem)
//    }
//  }
//
//  def enqueueCommand(payload: ControlPayload, from: ActorVirtualIdentity): Unit = {
//    if (recoveryQueue.isReplayCompleted) {
//      // may have race condition with restoreInput which happens inside DP thread.
//      lock.lock()
//      controlQueue.enqueue(ControlElement(payload, from))
//      lock.unlock()
//    } else {
//      recoveryQueue.addControl(ControlElement(payload, from))
//    }
//  }
//
//  def getDataElement: DataElement = {
//    if (recoveryQueue.isReplayCompleted) {
//      val elem = dataQueue.dequeue()
//      if (Constants.flowControlEnabled) {
//        elem match {
//          case InputTuple(from, _) =>
//            inputTuplesTakenOutOfQueue(from) =
//              inputTuplesTakenOutOfQueue.getOrElseUpdate(from, 0L) + 1
//          case _ =>
//          // do nothing
//        }
//      }
//      totalSteps += 1
//      //println(s"${totalSteps}: "+elem)
//      determinantLogger.stepIncrement()
//      elem
//    } else {
//      val elem = recoveryQueue.getData()
//      //println(s"${recoveryQueue.totalStep}: "+elem)
//      elem
//    }
//  }
//
//  def getControlElement: ControlElement = {
//    if (recoveryQueue.isReplayCompleted) {
//      controlQueue.dequeue()
//    } else {
//      recoveryQueue.getControl()
//    }
//  }
//
//  def getDataQueueLength: Int = dataQueue.size()
//
//  def getControlQueueLength: Int = controlQueue.size()
//
//  def restoreInputs(): Unit = {
//    lock.lock()
//    recoveryQueue.drainAllStashedElements(dataQueue, controlQueue)
//    lock.unlock()
//  }
//
//  def isControlQueueNonEmptyOrPaused: Boolean = {
//    if (recoveryQueue.isReplayCompleted) {
//      val res = !controlQueue.isEmpty || pauseManager.isPaused()
//      if(!res){
//        totalSteps += 1
//        determinantLogger.stepIncrement()
//      }
//      //println(s"${totalSteps}: check control")
//      res
//    } else {
//      val res = recoveryQueue.isReadyToEmitNextControl
//      //println(s"${recoveryQueue.totalStep}: check control")
//      res
//    }
//  }
//
//  def isDataQueueNonEmpty:Boolean = {
//    if(recoveryQueue.isReplayCompleted){
//      !dataQueue.isEmpty
//    }else{
//      !recoveryQueue.isReadyToEmitNextControl
//    }
//  }
//
//}
