package edu.uci.ics.amber.engine.common.virtualidentity

object ActorVirtualIdentity {
  case class WorkerActorVirtualIdentity(name: String) extends ActorVirtualIdentity {
    override def toString: String = "worker-" + name
  }
  case class ControllerVirtualIdentity() extends ActorVirtualIdentity {
    override def toString: String = "controller"
  }
  case class SelfVirtualIdentity() extends ActorVirtualIdentity
  case class ClientVirtualIdentity() extends ActorVirtualIdentity {
    override def toString: String = "client"
  }

  lazy val Controller: ControllerVirtualIdentity = ControllerVirtualIdentity()
  lazy val Self: SelfVirtualIdentity = SelfVirtualIdentity()
  lazy val Client: ClientVirtualIdentity = ClientVirtualIdentity()
}

trait ActorVirtualIdentity extends VirtualIdentity
