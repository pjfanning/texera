package edu.uci.ics.amber.engine.architecture.worker.processing

import akka.serialization.SerializationExtension
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.DeterminantLogReader
import edu.uci.ics.amber.engine.architecture.logging.{RecordedPayload, StepsOnChannel}
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager._
import edu.uci.ics.amber.engine.architecture.recovery.{InternalPayloadManager, PendingCheckpoint, RecoveryInternalQueueImpl, ReplayOrderEnforcer}
import edu.uci.ics.amber.engine.architecture.worker.{WorkerInternalQueue, WorkerInternalQueueImpl, WorkflowWorker}
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.{EstimationCompleted, ReplayCompleted, RuntimeCheckpointCompleted}
import edu.uci.ics.amber.engine.common.{AmberLogging, CheckpointSupport, ambermessage}
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, IdempotentInternalPayload, MarkerAlignmentInternalPayload, MarkerCollectionSupport, OneTimeInternalPayload, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class WorkerInternalPayloadManager(worker:WorkflowWorker) extends InternalPayloadManager with AmberLogging {

  override def actorId: ActorVirtualIdentity = worker.actorId

  override def handlePayload(channel:ChannelEndpointID, payload: IdempotentInternalPayload): Unit = {
    payload match {
      case ShutdownDP() =>
        worker.dataProcessor.dpThread.stop()
      case SetupLogging() =>
        InternalPayloadManager.setupLoggingForWorkflowActor(worker, true)
      case NoOp() =>
        // no op
      case _ => ???
    }
  }

  override def handlePayload(payload: OneTimeInternalPayload): Unit = {
    payload match {
      case EstimateCheckpointCost(id) =>
        worker.executeThroughDP(() =>{
          worker.dataProcessor.outputPort.broadcastMarker(payload)
          var estimatedCheckpointCost = 0
          if(worker.dataProcessor.operatorOpened) {
            worker.dataProcessor.operator match {
              case support: CheckpointSupport =>
                estimatedCheckpointCost = support.getEstimatedCheckpointTime
              case _ =>
            }
          }
          val stats = CheckpointStats(
            worker.dataProcessor.cursor.getStep,
            worker.inputPort.getFIFOState,
            worker.dataProcessor.outputPort.getFIFOState,
            0,
            estimatedCheckpointCost + worker.dataProcessor.internalQueue.getDataQueueLength)
          worker.dataProcessor.outputPort.sendToClient(EstimationCompleted(actorId, id, stats))
        })
      case LoadStateAndReplay(id, fromCheckpoint, replayTo, confs) =>
        worker.isReplaying = true
        var existingChkpt:SavedCheckpoint = null
        var outputIter:Iterator[(ITuple, Option[Int])] = Iterator.empty
        worker.dpThread.stop() // intentionally kill DP
        if(fromCheckpoint.isDefined){
          existingChkpt = CheckpointHolder.getCheckpoint(worker.actorId, fromCheckpoint.get)
          existingChkpt.attachSerialization(SerializationExtension(worker.context.system))
          worker.inputPort.setFIFOState(existingChkpt.load("fifoState"))
          logger.info("fifo state restored")
          worker.internalQueue = existingChkpt.load("internalQueue")
          logger.info(s"input queue restored")
          worker.dataProcessor = existingChkpt.load("dataProcessor")
          logger.info(s"DP restored")
          if(worker.dataProcessor.operatorOpened) {
            worker.operator match {
              case support: CheckpointSupport =>
                outputIter = support.deserializeState(existingChkpt)
              case _ =>
            }
          }
          logger.info(s"operator restored current step = ${worker.dataProcessor.cursor.getStep}")
          worker.dataProcessor.initDP(
            worker,
            outputIter
          )
        }
        // setup replay infra
        val logReader = InternalPayloadManager.retrieveLogForWorkflowActor(worker)
        val replayOrderEnforcer = setupReplay(logReader)
        transferQueueContent(replayOrderEnforcer)
        if(replayTo.isDefined){
          val currentStep = worker.dataProcessor.cursor.getStep
          replayOrderEnforcer.setReplayTo(currentStep, replayTo.get,  () => {
            logger.info("replay completed, waiting for next instruction")
            worker.dpThread.blockingOnNextStep()
            worker.dataProcessor.outputPort.sendToClient(ReplayCompleted(actorId, id))
          })
          worker.dpThread.unblock() // in case it is blocked.
        }
        // setup checkpoints during replay
        // create empty checkpoints to fill
        confs.foreach(conf => {
          val planned = new SavedCheckpoint()
          planned.attachSerialization(SerializationExtension(worker.context.system))
          val pendingCheckpoint = new PendingCheckpoint(
            conf.estimationId,
            worker.actorId,
            0,
            conf.checkpointAt,
            Map.empty,
            Map.empty,
            0,
            planned,
            conf.waitingForMarker)
          addPendingCheckpoint(conf.id, pendingCheckpoint)
          logger.info(s"set future checkpoint at step ${conf.checkpointAt}")
          replayOrderEnforcer.setCheckpoint(conf.checkpointAt, () =>{
            // now inside DP thread
            worker.dpThread.dpInterrupted{
              logger.info(s"taking checkpoint during replay at step ${worker.dataProcessor.cursor.getStep}")
              pendingCheckpoint.recordingLock.lock()
              worker.dataProcessor.outputPort.broadcastMarker(TakeRuntimeGlobalCheckpoint(conf.id, Map.empty))
              fillCheckpoint(planned)
              pendingCheckpoint.startRecording = true
              pendingCheckpoint.recordingLock.unlock()
            }
          })
        })
        // disable logging
        InternalPayloadManager.setupLoggingForWorkflowActor(worker, false)
        // put recorded input payload from checkpoint back to queue
        worker.dpThread = new DPThread(actorId, worker.dataProcessor, worker.internalQueue, replayOrderEnforcer)
        logger.info("starting new DP thread...")
        assert(worker.internalQueue.isInstanceOf[RecoveryInternalQueueImpl])
        logger.info(s"worker restored! input Seq: ${worker.inputPort.getFIFOState}")
        logger.info(s"worker restored! output Seq: ${worker.dataProcessor.outputPort.getFIFOState}")
        worker.dpThread.start() // new DP is not started yet.
        if(existingChkpt != null){
          logger.info(s"recorded data: ${existingChkpt.getInputData.map(x => s"${x._1} -> ${x._2.size}")}")
          existingChkpt.getInputData.foreach{
            case (c, payloads) =>
              logger.info(s"restore input for channel $c, number of payload = ${payloads.size}")
              payloads.foreach(x => worker.inputPort.handleFIFOPayload(c, x))
          }
        }
        addRecordedPayloadBack(logReader)
      case _ => ???
    }
  }


  def transferQueueContent(replayOrderEnforcer: ReplayOrderEnforcer): Unit ={
    val recoveryQueue = new RecoveryInternalQueueImpl(worker.actorId, worker.creditMonitor, replayOrderEnforcer)
    WorkerInternalQueue.transferContent(worker.internalQueue, recoveryQueue)
    worker.internalQueue = recoveryQueue
  }

  def addRecordedPayloadBack(logReader: DeterminantLogReader): Unit ={
    // add recorded payload back to the queue from log
    val recoveredSeqMap = mutable.HashMap[ChannelEndpointID, Long]()
    logReader.getLogs[RecordedPayload].foreach(elem => {
      val seqNum = recoveredSeqMap.getOrElseUpdate(elem.channel, 0L)
      val message = WorkflowFIFOMessage(elem.channel, seqNum, elem.payload)
      worker.inputPort.handleMessage(message) // following FIFO
      recoveredSeqMap(elem.channel) += 1
    })
  }

  def setupReplay(logReader: DeterminantLogReader): ReplayOrderEnforcer ={
    val replayOrderEnforcer = new ReplayOrderEnforcer(logReader.getLogs[StepsOnChannel], () => {
      logger.info("recovery completed, continue normal processing")
      val normalQueue = new WorkerInternalQueueImpl(worker.creditMonitor)
      WorkerInternalQueue.transferContent(worker.internalQueue, normalQueue)
      worker.internalQueue = normalQueue
    })
    val currentStep = worker.dataProcessor.cursor.getStep
    replayOrderEnforcer.initialize(currentStep)
    replayOrderEnforcer
  }

  def fillCheckpoint(chkpt: SavedCheckpoint): Long ={
    val startTime = System.currentTimeMillis()
    logger.info("start to take checkpoint")
    chkpt.save("fifoState", worker.inputPort.getFIFOState)
    chkpt.save("internalQueue", worker.internalQueue)
    if(worker.dataProcessor.operatorOpened){
      worker.dataProcessor.operator match {
        case support: CheckpointSupport =>
          worker.dataProcessor.outputIterator.setTupleOutput(
            support.serializeState(worker.dataProcessor.outputIterator.outputIter, chkpt)
          )
        case _ =>
      }
    }
    chkpt.save("dataProcessor", worker.dataProcessor)
    System.currentTimeMillis() - startTime
  }

  override def markerAlignmentStart(payload: MarkerAlignmentInternalPayload): MarkerCollectionSupport = {
    payload match {
      case TakeRuntimeGlobalCheckpoint(id, alignmentMap) =>
        worker.executeThroughDP(() =>{
          val chkpt = new SavedCheckpoint()
          chkpt.attachSerialization(SerializationExtension(worker.context.system))
          worker.dataProcessor.outputPort.broadcastMarker(payload)
          val elapsed = fillCheckpoint(chkpt)
          val pending = new PendingCheckpoint(
            id,
            worker.actorId,
            System.currentTimeMillis(),
            worker.dataProcessor.cursor.getStep,
            worker.inputPort.getFIFOState,
            worker.dataProcessor.outputPort.getFIFOState,
            elapsed,
            chkpt,
            alignmentMap(worker.actorId))
            pending.startRecording = true
          pending
        })
      case _ => ???
    }
  }

  override def markerAlignmentEnd(payload: MarkerAlignmentInternalPayload, support: MarkerCollectionSupport): Unit = {
    payload match {
      case TakeRuntimeGlobalCheckpoint(id, _) =>
        worker.executeThroughDP(() =>{
          val pendingCheckpoint = support.asInstanceOf[PendingCheckpoint]
          CheckpointHolder.addCheckpoint(
            actorId,
            pendingCheckpoint.stepCursorAtCheckpoint,
            pendingCheckpoint.chkpt
          )
          logger.info(
            s"local checkpoint completed! initial time spent = ${pendingCheckpoint.initialCheckpointTime / 1000f}s alignment time = ${(System.currentTimeMillis() - pendingCheckpoint.startTime) / 1000f}s"
          )
          val alignmentCost = System.currentTimeMillis() - pendingCheckpoint.startTime
          val stats = CheckpointStats(
            pendingCheckpoint.stepCursorAtCheckpoint,
            pendingCheckpoint.fifoInputState,
            pendingCheckpoint.fifoOutputState,
            alignmentCost,
            pendingCheckpoint.initialCheckpointTime)
          worker.dataProcessor.outputPort.sendToClient(RuntimeCheckpointCompleted(actorId, pendingCheckpoint.checkpointId, stats))
        })
      case _ => ???
    }
  }
}
