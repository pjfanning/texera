package edu.uci.ics.amber.engine.architecture.control.utils

import edu.uci.ics.amber.engine.common.rpc.{
  AsyncRPCClient,
  AsyncRPCService,
  AsyncRPCServer
}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

class TesterAsyncRPCService(
    val myID: ActorVirtualIdentity,
    source: AsyncRPCClient,
    receiver: AsyncRPCServer
) extends AsyncRPCService(source, receiver)
    with PingPongHandler
    with ChainHandler
    with MultiCallHandler
    with CollectHandler
    with NestedHandler
    with RecursionHandler
    with ErrorHandler {}
