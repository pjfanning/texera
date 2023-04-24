package edu.uci.ics.amber.engine.architecture.worker.processing

import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.recovery.ReplayOrderEnforcer
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.{READY, UNINITIALIZED}
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{ControlPayload, DPMessage, DataPayload, FuncDelegate}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import edu.uci.ics.amber.error.ErrorUtils.safely

import java.util.concurrent.{CompletableFuture, ExecutorService, Executors, Future}

class DPThread(val actorId: ActorVirtualIdentity,
               dp:DataProcessor,
               var internalQueue: WorkerInternalQueue,
               replayOrderEnforcer: ReplayOrderEnforcer = null) extends AmberLogging{

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
          endFuture.complete(Unit)
        }
      })
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
        replayOrderEnforcer.forwardReplayProcess(dp.cursor.getStep)
      }
      // block here to let main thread do following.
      // 1) replace queue from replay to normal 2) continue replay
      if(!waitFuture.isDone){
        dpInterrupted{
          logger.info("DP Thread is blocked, waiting for unblock")
          waitFuture.get()
        }
      }
      if ((dp.hasUnfinishedInput || dp.hasUnfinishedOutput) && !dp.pauseManager.isPaused()) {
        val input = internalQueue.peek()
        input match {
          case Some(DPMessage(_, delegate: FuncDelegate[_])) =>
            // received system message
            dpInterrupted {
              internalQueue.take() // take the message out
              delegate.future.complete(delegate.func().asInstanceOf[delegate.returnType])
            }
          case None =>
            dp.continueDataProcessing()
          case Some(msg: DPMessage) if !msg.channel.isControlChannel =>
            dp.continueDataProcessing()
          case Some(msg: DPMessage) if msg.channel.isControlChannel =>
            val controlMsg = internalQueue.take()
            dp.processControlPayload(controlMsg.channel, controlMsg.payload.asInstanceOf[ControlPayload])
          case other =>
            throw new RuntimeException(s"DP thread cannot handle message $other")
        }
      } else {
        val msg = internalQueue.take()
        msg.payload match {
          case data: DataPayload =>
            dp.processDataPayload(msg.channel, data)
          case control: ControlPayload =>
            dp.processControlPayload(msg.channel, control)
          case delegate: FuncDelegate[_] =>
            // received system message
            dpInterrupted {
              delegate.future.complete(delegate.func().asInstanceOf[delegate.returnType])
            }
          case other =>
            throw new RuntimeException(s"DP thread cannot handle message $other")
        }
      }
    }
  }



}
