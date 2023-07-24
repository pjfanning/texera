package edu.uci.ics.amber.engine.architecture.worker.processing

import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.recovery.ReplayOrderEnforcer
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.DPSynchronized
import edu.uci.ics.amber.engine.architecture.worker.{WorkerInternalQueue, WorkflowWorker}
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.{READY, UNINITIALIZED}
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{ControlPayload, DPMessage, DataPayload, StartSync}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import edu.uci.ics.amber.error.ErrorUtils.safely

import java.util.concurrent.{CompletableFuture, ExecutorService, Executors, Future}

class DPThread(val actorId: ActorVirtualIdentity,
               dp:DataProcessor,
               var internalQueue: WorkerInternalQueue,
               worker:WorkflowWorker,
               replayOrderEnforcer: ReplayOrderEnforcer = null) extends AmberLogging{

  // initialize dp thread upon construction
  @transient
  var dpThreadExecutor: ExecutorService = _
  @transient
  var dpThread: Future[_] = _

  def getThreadName:String = "DP-thread"

  private val endFuture = new CompletableFuture[Unit]()

  var blockingFuture = new CompletableFuture[Unit]()

  def stop(): Unit ={
    if(dpThread != null){
      dpThread.cancel(true) // interrupt
      stopped = true
      endFuture.get()
    }
    if(dpThreadExecutor != null){
      dpThreadExecutor.shutdownNow() // destroy thread
    }
  }

  @volatile
  private var stopped = false

  def start(): Unit = {
    if (dpThreadExecutor != null) {
      logger.info("DP Thread is already running")
      return
    }
    dpThreadExecutor = Executors.newSingleThreadExecutor
    if (dp.stateManager.getCurrentState == UNINITIALIZED) {
      dp.stateManager.transitTo(READY)
    }
    if (dpThread == null) {
      // TODO: setup context
      // operator.context = new OperatorContext(new TimeService(logManager))
      val startFuture = new CompletableFuture[Unit]()
      dpThread = dpThreadExecutor.submit(new Runnable() {
        def run(): Unit = {
          Thread.currentThread().setName(getThreadName)
          logger.info("DP thread started")
          startFuture.complete(Unit)
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
          endFuture.complete(Unit)
        }
      })
      startFuture.get()
    }
  }

  def dpInterrupted(code: => Unit): Unit ={
    dp.onInterrupt()
    code
    dp.onContinue()
  }

  @throws[Exception]
  private[this] def runDPThreadMainLogicNew(): Unit = {
    // main DP loop
    while (!stopped) {
      if(replayOrderEnforcer != null){
        val currentStep = dp.cursor.getStep
        replayOrderEnforcer.triggerCallbacks(currentStep)
        replayOrderEnforcer.forwardReplayProcess(currentStep)
      }
      if ((dp.hasUnfinishedInput || dp.hasUnfinishedOutput) && !dp.pauseManager.isPaused()) {
        val input = internalQueue.peek(dp)
        input match {
          case None =>
            dp.continueDataProcessing()
          case Some(msg: DPMessage) if !msg.channel.isControlChannel =>
            dp.continueDataProcessing()
          case Some(msg: DPMessage) if msg.channel.isControlChannel =>
            val controlOrSystemMsg = internalQueue.take(dp)
            controlOrSystemMsg match{
              case DPMessage(channel, delegate: StartSync) =>
                // received sync request
                dpInterrupted {
                  worker.context.self ! DPSynchronized()
                  blockingFuture.get()
                  blockingFuture = new CompletableFuture[Unit]() // reset
                }
              case DPMessage(channel, payload:ControlPayload) =>
                dp.processControlPayload(channel, payload)
            }
          case other =>
            throw new RuntimeException(s"DP thread cannot handle message $other")
        }
      } else {
        val msg = internalQueue.take(dp)
        msg.payload match {
          case data: DataPayload =>
            // logger.info(s"taking message from channel = ${msg.channel} at step = ${dp.cursor.getStep} batchSize = ${if(dp.inputBatch==null)0 else{dp.inputBatch.length}}, batch tuple = ${if(dp.inputBatch==null || dp.inputBatch.isEmpty)"null" else{dp.inputBatch.head.toString}}")
            dp.processDataPayload(msg.channel, data)
          case control: ControlPayload =>
            dp.processControlPayload(msg.channel, control)
          case delegate: StartSync =>
            // received sync request
            dpInterrupted {
              worker.context.self ! DPSynchronized()
              blockingFuture.get()
              blockingFuture = new CompletableFuture[Unit]() // reset
            }
          case other =>
            throw new RuntimeException(s"DP thread cannot handle message $other")
        }
      }
    }
  }



}
