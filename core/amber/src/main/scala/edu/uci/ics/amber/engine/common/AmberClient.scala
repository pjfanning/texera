package edu.uci.ics.amber.engine.common

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.pattern._
import akka.util.Timeout
import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.{Controller, ControllerConfig, Workflow}
import edu.uci.ics.amber.engine.common.ClientActor.{
  ClosureRequest,
  CommandRequest,
  InitializeRequest,
  ObservableRequest
}
import edu.uci.ics.amber.engine.common.FutureBijection._
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import rx.lang.scala.{Observable, Subject}

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, DurationInt}
import scala.reflect.ClassTag

class AmberClient(system: ActorSystem, workflow: Workflow, controllerConfig: ControllerConfig) {

  private val client = system.actorOf(Props(new ClientActor))
  private implicit val timeout: Timeout = Timeout(1.minute)
  private val registeredObservables = new mutable.HashMap[Class[_], Observable[_]]()
  @volatile private var isActive = true

  Await.result(client ? InitializeRequest(workflow, controllerConfig), 10.seconds)

  def shutdown(): Unit = {
    if (isActive) {
      isActive = false
      client ! PoisonPill
    }
  }

  def sendAsync[T](controlCommand: ControlCommand[T]): Future[T] = {
    if (!isActive) {
      Future.exception(new RuntimeException("amber runtime environment is not active"))
    } else {
      (client ? CommandRequest(controlCommand)).asTwitter().asInstanceOf[Future[T]]
    }
  }

  def sendSync[T](controlCommand: ControlCommand[T], deadline: Duration = timeout.duration): T = {
    if (!isActive) {
      throw new RuntimeException("amber runtime environment is not active")
    } else {
      Await.result(client ? CommandRequest(controlCommand), deadline).asInstanceOf[T]
    }
  }

  def fireAndForget[T](controlCommand: ControlCommand[T]): Unit = {
    if (!isActive) {
      throw new RuntimeException("amber runtime environment is not active")
    } else {
      client ! CommandRequest(controlCommand)
    }
  }

  def getObservable[T](implicit ct: ClassTag[T]): Observable[T] = {
    if (!isActive) {
      throw new RuntimeException("amber runtime environment is not active")
    }
    assert(
      client.path.address.hasLocalScope,
      "get observable with a remote client actor is not supported"
    )
    val clazz = ct.runtimeClass
    if (registeredObservables.contains(clazz)) {
      return registeredObservables(clazz).asInstanceOf[Observable[T]]
    }
    val sub = Subject[T]
    val req = ObservableRequest({
      case x: T =>
        sub.onNext(x)
    })
    Await.result(client ? req, 2.seconds)
    val ob = sub.onTerminateDetach
    registeredObservables(clazz) = ob
    ob
  }

  def executeClosureSync[T](closure: => T): T = {
    if (!isActive) {
      closure
    } else {
      Await.result(client ? ClosureRequest(() => closure), timeout.duration).asInstanceOf[T]
    }
  }

}
