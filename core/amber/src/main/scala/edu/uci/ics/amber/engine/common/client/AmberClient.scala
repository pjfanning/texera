package edu.uci.ics.amber.engine.common.client

import akka.actor.{ActorSystem, Address, PoisonPill, Props}
import akka.pattern._
import akka.util.Timeout
import com.twitter.util.{Future, Promise}
import edu.uci.ics.amber.engine.architecture.controller.{ControllerConfig, Workflow}
import edu.uci.ics.amber.engine.common.client.ClientActor.{
  CommandRequest,
  ExecuteRequest,
  InitializeRequest,
  ObservableRequest
}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.texera.workflow.common.workflow.PipelinedRegionPlan
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.reflect.ClassTag

class AmberClient(
    system: ActorSystem,
    workflowGen: () => (Workflow, PipelinedRegionPlan),
    controllerConfig: ControllerConfig,
    errorHandler: Throwable => Unit
) {

  private val clientActor = system.actorOf(Props(new ClientActor))
  private implicit val timeout: Timeout = Timeout(1.minute)
  private val registeredObservables = new mutable.HashMap[Class[_], Observable[_]]()
  @volatile private var isActive = true

  Await.result(clientActor ? InitializeRequest(workflowGen(), controllerConfig), 10.seconds)

  def shutdown(): Unit = {
    if (isActive) {
      isActive = false
      clientActor ! PoisonPill
      registeredObservables.clear()
    }
  }

  def sendAsync[T](controlCommand: ControlCommand[T]): Future[T] = {
    if (!isActive) {
      Future.exception(new RuntimeException("amber runtime environment is not active"))
    } else {
      val promise = Promise[Any]
      clientActor ! CommandRequest(controlCommand, promise)
      promise.map(ret => ret.asInstanceOf[T])
    }
  }

  def sendAsyncWithCallback[T](controlCommand: ControlCommand[T], callback: T => Unit): Unit = {
    if (!isActive) {
      Future.exception(new RuntimeException("amber runtime environment is not active"))
    } else {
      val promise = Promise[Any]
      promise.onSuccess { value =>
        callback(value.asInstanceOf[T])
      }
      promise.onFailure(t => errorHandler(t))
      clientActor ! CommandRequest(controlCommand, promise)
    }
  }

  def fireAndForget[T](controlCommand: ControlCommand[T]): Unit = {
    if (!isActive) {
      throw new RuntimeException("amber runtime environment is not active")
    } else {
      clientActor ! controlCommand
    }
  }

  def notifyNodeFailure(address: Address): Future[Unit] = {
    if (isActive) {
      println(s"received node failure")
      // get actor ref of existing workers on that node
      // setup recovery
    }
    Future.Unit
  }

  def executeAsync(closure: (ClientActor) => Unit): Unit = {
    clientActor ! ExecuteRequest(closure)
  }

//  def replayExecution(requestId:String, conf: WorkflowReplayConfig): Unit = {
//    if (isActive) {
//      println(s"received replay request conf = ${conf}")
//      clientActor ! ReplayWorkflow(requestId, conf)
//    }
//  }
//
//  def takeGlobalCheckpoint(): Unit = {
//    if (!isActive) {
//      Future[Any](())
//    } else {
//      clientActor ! TakeRuntimeGlobalCheckpoint(CheckpointHolder.generateCheckpointId, Map.empty)
//    }
//  }

//  def interruptReplay(): Future[Any] = {
//    if (!isActive) {
//      Future[Any](())
//    } else {
//      (clientActor ? AmberInternalMessage(CLIENT, InterruptReplay())).asTwitter()
//    }
//    Future()
//  }

  def registerCallback[T](callback: T => Unit)(implicit ct: ClassTag[T]): Disposable = {
    if (!isActive) {
      throw new RuntimeException("amber runtime environment is not active")
    }
    assert(
      clientActor.path.address.hasLocalScope,
      "get observable with a remote client actor is not supported"
    )
    val clazz = ct.runtimeClass
    val observable =
      if (registeredObservables.contains(clazz)) {
        registeredObservables(clazz).asInstanceOf[Observable[T]]
      } else {
        val sub = PublishSubject.create[T]()
        val req = ObservableRequest({
          case x: T =>
            sub.onNext(x)
        })
        Await.result(clientActor ? req, atMost = 2.seconds)
        val ob = sub.onTerminateDetach
        registeredObservables(clazz) = ob
        ob
      }
    observable.subscribe { evt: T =>
      {
        try {
          callback(evt)
        } catch {
          case t: Throwable => errorHandler(t)
        }
      }
    }
  }

}
