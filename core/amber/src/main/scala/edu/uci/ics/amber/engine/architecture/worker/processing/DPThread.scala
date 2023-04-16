package edu.uci.ics.amber.engine.architecture.worker.processing

import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.DPMessage
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.{READY, UNINITIALIZED}
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, DataPayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import edu.uci.ics.amber.error.ErrorUtils.safely

import java.util.concurrent.{ExecutorService, Executors, Future}

class DPThread(val actorId: ActorVirtualIdentity,
               dp:DataProcessor,
               internalQueue: WorkerInternalQueue) extends AmberLogging{

  // initialize dp thread upon construction
  @transient
  private[processing] var dpThreadExecutor: ExecutorService = _
  @transient
  private[processing] var dpThread: Future[_] = _

  def stop(): Unit ={
    dpThread.cancel(true) // interrupt
    dpThreadExecutor.shutdownNow() // destroy thread
    stopped = true
  }

  @volatile
  private var stopped = false

  def start(): Unit = {
    if (dpThreadExecutor != null) {
      return
    }
    dp.attachDPThread(this)
    dpThreadExecutor = Executors.newSingleThreadExecutor
    if (dp.stateManager.getCurrentState == UNINITIALIZED) {
      dp.stateManager.transitTo(READY)
    }
    if (dpThread == null) {
      // TODO: setup context
      // operator.context = new OperatorContext(new TimeService(logManager))
      dpThread = dpThreadExecutor.submit(new Runnable() {
        def run(): Unit = {
          logger.info("DP thread started")
          try {
            runDPThreadMainLogicNew()
          } catch safely {
            case _: InterruptedException =>
              // dp thread will stop here
              logger.info("DP Thread exits")
            case err: Exception =>
              logger.error("DP Thread exists unexpectedly", err)
              dp.asyncRPCClient.send(
                FatalError(new WorkflowRuntimeException("DP Thread exists unexpectedly", err)),
                CONTROLLER
              )
          }
        }
      })
    }
  }

  @throws[Exception]
  private[this] def runDPThreadMainLogicNew(): Unit = {
    // main DP loop
    while (!stopped) {
      val currentStep = dp.determinantLogger.getStep
      var skipStepIncrement = false
      if ((dp.hasUnfinishedInput || dp.hasUnfinishedOutput) && !dp.pauseManager.isPaused()) {
        val input = internalQueue.peek(currentStep)
        input match {
          case None | Some(DPMessage(ChannelEndpointID(_, false), _)) =>
            dp.continueDataProcessing()
          case Some(DPMessage(ChannelEndpointID(_, true), _))=>
            val controlMsg = internalQueue.take(currentStep)
            dp.updateInputChannel(controlMsg.channel)
            skipStepIncrement = dp.processControlPayload(controlMsg.channel, controlMsg.payload.asInstanceOf[ControlPayload])
        }
      } else {
        val msg = internalQueue.take(currentStep)
        dp.updateInputChannel(msg.channel)
        msg.payload match {
          case data: DataPayload =>
            dp.handleDataPayload(msg.channel, data)
          case control: ControlPayload =>
            skipStepIncrement = dp.processControlPayload(msg.channel, control)
        }
      }
      if(!skipStepIncrement){
        dp.determinantLogger.stepIncrement()
      }
    }
  }



}
