package edu.uci.ics.amber.engine.common.rpc

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkOutputPort
import edu.uci.ics.amber.engine.common.rpcwrapper.ControlPayload
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import io.grpc.Channel

abstract class AsyncRPCService[T](actorId: ActorVirtualIdentity, outputPort: NetworkOutputPort[ControlPayload]) {

  def serviceMethodMapping:Map[String, Any => Future[_]]

  def serviceStubGen: (Channel) => T

  val context:RpcContext = new RpcContext()

  private[this] val client = new AsyncRPCClient[T](outputPort, actorId) {
    override def stubGen: Channel => T = serviceStubGen

    override def getContext: RpcContext = context
  }

  private[this] val server = new AsyncRPCServer(outputPort, actorId) {
    override def getContext: RpcContext = context

    override def methodMapping: Map[String, Any => Future[_]] = serviceMethodMapping
  }

  def getStub(dest: ActorVirtualIdentity): T ={
    client.getStub(dest)
  }

}
