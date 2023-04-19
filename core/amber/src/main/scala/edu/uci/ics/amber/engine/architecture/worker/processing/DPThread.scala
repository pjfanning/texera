package edu.uci.ics.amber.engine.architecture.worker.processing

import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.{READY, UNINITIALIZED}
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, DPMessage, DataPayload, FuncDelegate}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import edu.uci.ics.amber.error.ErrorUtils.safely

import java.util.concurrent.{CompletableFuture, ExecutorService, Executors, Future}

class DPThread(val actorId: ActorVirtualIdentity,
               dp:DataProcessor,
               var internalQueue: WorkerInternalQueue) extends AmberLogging{

  // initialize dp thread upon construction
  @transient
  var dpThreadExecutor: ExecutorService = _
  @transient
  var dpThread: Future[_] = _

  private val endFuture = new CompletableFuture[Unit]()

  private var waitFuture = CompletableFuture.completedFuture[Unit]()

  def blockingOnNextStep(): Unit ={
    waitFuture = new CompletableFuture[Unit]()
  }

  def unblock(): Unit ={
    waitFuture.complete(Unit)
  }

  def stop(): Unit ={
    dpThread.cancel(true) // interrupt
    dpThreadExecutor.shutdownNow() // destroy thread
    stopped = true
    endFuture.get()
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
      // block here to let main thread do following.
      // 1) replace queue from replay to normal 2) continue replay
      waitFuture.get()
      if ((dp.hasUnfinishedInput || dp.hasUnfinishedOutput) && !dp.pauseManager.isPaused()) {
        val input = internalQueue.peek(currentStep)
        input match {
          case Some(DPMessage(_, delegate: FuncDelegate[_])) =>
            // received system message
            internalQueue.take(currentStep) // take the message out
            delegate.future.complete(delegate.func().asInstanceOf[delegate.returnType])
          case None =>
            dp.continueDataProcessing()
          case Some(msg: DPMessage) if !msg.channel.isControlChannel =>
            dp.continueDataProcessing()
          case Some(msg: DPMessage) if msg.channel.isControlChannel =>
            val controlMsg = internalQueue.take(currentStep)
            dp.processControlPayload(controlMsg.channel, controlMsg.payload.asInstanceOf[ControlPayload])
          case other =>
            throw new RuntimeException(s"DP thread cannot handle message $other")
        }
      } else {
        val msg = internalQueue.take(currentStep)
        msg.payload match {
          case data: DataPayload =>
            dp.processDataPayload(msg.channel, data)
          case control: ControlPayload =>
            dp.processControlPayload(msg.channel, control)
          case delegate: FuncDelegate[_] =>
            // received system message
            delegate.future.complete(delegate.func().asInstanceOf[delegate.returnType])
          case other =>
            throw new RuntimeException(s"DP thread cannot handle message $other")
        }
      }
    }
    endFuture.complete(Unit)
  }



}
