package edu.uci.ics.amber.engine.architecture.recovery

import akka.serialization.SerializationExtension
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.DeterminantLogReader
import edu.uci.ics.amber.engine.architecture.worker.{ReplayCheckpointConfig, WorkerInternalQueue, WorkerInternalQueueImpl}
import edu.uci.ics.amber.engine.common.{AmberLogging, CheckpointSupport}
import edu.uci.ics.amber.engine.common.ambermessage.{AmberInternalPayload, ChannelEndpointID, MarkerCollectionSupport, WorkflowFIFOMessage, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

abstract class CheckpointRestoreManager(@transient actor:WorkflowActor) extends AmberLogging{

  override def actorId: ActorVirtualIdentity = actor.actorId

  protected def overwriteState(chkpt:SavedCheckpoint):Unit

  protected def setupReplay(replayId:String, logReader: DeterminantLogReader, replayTo:Option[Long]):ReplayOrderEnforcer

  def fillCheckpoint(checkpoint: PendingCheckpoint):Long

  protected def doCheckpointDuringReplay(pendingCheckpoint: PendingCheckpoint, conf:ReplayCheckpointConfig): () => Unit

  protected def startProcessing(stateReloaded:Boolean, replayOrderEnforcer: ReplayOrderEnforcer):Unit

  def restoreFromCheckpointAndSetupReplay(replayId:String, fromCheckpoint:Option[Long], fifoState:Map[ChannelEndpointID, Long], replayTo:Option[Long], confs:Array[ReplayCheckpointConfig], pendingCheckpoints:mutable.HashMap[String, MarkerCollectionSupport]): Unit ={
    var recordedInput = mutable.Map[ChannelEndpointID, mutable.ArrayBuffer[WorkflowFIFOMessagePayload]]()
    if(fromCheckpoint.isDefined){
      actor.inputPort.setFIFOState(fifoState)
      val existingChkpt = CheckpointHolder.getCheckpoint(actor.actorId, fromCheckpoint.get)
      existingChkpt.attachSerialization(SerializationExtension(actor.context.system))
      overwriteState(existingChkpt)
      recordedInput = existingChkpt.getInputData
      logger.info(s"recorded data: ${recordedInput.map(x => s"${x._1} -> ${x._2.size}")}")
    }
    val logReader = InternalPayloadManager.retrieveLogForWorkflowActor(actor)
    val orderEnforcer = setupReplay(replayId, logReader, replayTo)
    confs.foreach(conf => {
      // setup checkpoints during replay
      // create empty checkpoints to fill
      val planned = new SavedCheckpoint()
      planned.attachSerialization(SerializationExtension(actor.context.system))
      val pendingCheckpoint = new PendingCheckpoint(
        conf.estimationId,
        actor.actorId,
        0,
        conf.checkpointAt,
        Map.empty,
        Map.empty,
        0,
        planned,
        conf.waitingForMarker)
      // add this checkpoint to pending checkpoints
      pendingCheckpoints(conf.id) = pendingCheckpoint
      orderEnforcer.setCheckpoint(conf.checkpointAt, doCheckpointDuringReplay(pendingCheckpoint, conf))
    })
    InternalPayloadManager.setupLoggingForWorkflowActor(actor, false)
    recordedInput.foreach{
      case (c, payloads) =>
        logger.info(s"restore input for channel $c, number of payload = ${payloads.size}")
        payloads.foreach(x => actor.handlePayloadAndMarker(c, x))
    }
    startProcessing(fromCheckpoint.isDefined, orderEnforcer)
  }

}
