package edu.uci.ics.amber.engine.architecture.common

import akka.actor.{ActorContext, ActorRef, Cancellable, Props}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.RegisterActorRef
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import akka.pattern.ask
import akka.util.Timeout

class WorkflowActorService(actor:WorkflowActor) {

  private val actorContext:ActorContext = actor.context

  var networkCommunicationActor: ActorRef = actor.networkCommunicationActor

  implicit def ec:ExecutionContext = actorContext.dispatcher
  implicit val timeout: Timeout = 5.seconds

  def self:ActorRef = actorContext.self

  def actorOf(props:Props): ActorRef ={
    actorContext.actorOf(props)
  }

  def registerActorForNetworkCommunication(workerId:ActorVirtualIdentity, ref:ActorRef): Unit ={
    Await.result(networkCommunicationActor ? RegisterActorRef(workerId, ref), 5.seconds)
  }

  def scheduleOnce(delay:FiniteDuration, callable:() => Unit):Cancellable ={
    actorContext.system.scheduler.scheduleOnce(delay){
      callable()
    }
  }

  def scheduleWithFixedDelay(initialDelay:FiniteDuration, delay:FiniteDuration, callable: ()=> Unit):Cancellable = {
    actorContext.system.scheduler.scheduleWithFixedDelay(initialDelay, delay)(() => callable())
  }

}
