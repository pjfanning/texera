package edu.uci.ics.amber.engine.architecture.worker.processing

import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.logging.{DeterminantLogger, LogManager}
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage
import edu.uci.ics.amber.engine.architecture.recovery.LocalRecoveryManager
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.{CONTROL_MESSAGE, DATA_MESSAGE, NO_MESSAGE}
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.{READY, UNINITIALIZED}
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{ControlPayload, DataPayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import edu.uci.ics.amber.error.ErrorUtils.safely

import java.util.concurrent.{ExecutorService, Executors, Future}

class DPThread(val actorId: ActorVirtualIdentity, logManager:LogManager) extends AmberLogging{

  // outer dependencies
  @transient
  private[processing] var internalQueue: WorkerInternalQueue = _
  @transient
  private[processing] var logStorage: DeterminantLogStorage = _
  @transient
  private[processing] var recoveryManager: LocalRecoveryManager = _

  // initialize dp thread upon construction
  @transient
  private[processing] var dpThreadExecutor: ExecutorService = _
  @transient
  private[processing] var dpThread: Future[_] = _

  private val dp = new DataProcessor()
  def start(): Unit = {
    if (dpThreadExecutor != null) {
      return
    }
    dpThreadExecutor = Executors.newSingleThreadExecutor
    if (dp.stateManager.getCurrentState == UNINITIALIZED) {
      dp.stateManager.transitTo(READY)
    }
    if (dpThread == null) {
      // TODO: setup context
      // operator.context = new OperatorContext(new TimeService(logManager))
      dpThread = dpThreadExecutor.submit(new Runnable() {
        def run(): Unit = {
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
    while (true) {
      val currentStep = dp.determinantLogger.getStep
      var skipStepIncrement = false
      if ((dp.hasUnfinishedInput || dp.hasUnfinishedOutput) && !dp.pauseManager.isPaused()) {
        val input = internalQueue.getQueueHeadStatus(currentStep)
        input match {
          case NO_MESSAGE | DATA_MESSAGE =>
            dp.continueDataProcessing()
          case CONTROL_MESSAGE =>
            val controlMsg = internalQueue.take(currentStep)
            dp.updateInputChannel(controlMsg.channel)
            skipStepIncrement = dp.processControlPayload(controlMsg.channel, controlMsg.payload.asInstanceOf[ControlPayload])
        }
      } else {
        internalQueue.enableDataQueue(dp.pauseManager.isPaused())
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
