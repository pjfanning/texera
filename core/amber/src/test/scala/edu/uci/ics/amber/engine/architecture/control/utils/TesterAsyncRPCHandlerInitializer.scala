package edu.uci.ics.amber.engine.architecture.control.utils

import edu.uci.ics.amber.engine.common.rpc.{
  AsyncRPCClient,
  AsyncRPCHandlerInitializer,
  AsyncRPCServer
}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

class TesterAsyncRPCHandlerInitializer(val tester: TrivialControlTester)
    extends AsyncRPCHandlerInitializer(tester.asyncRPCClient, tester.asyncRPCServer)
    with PingPongHandler
    with ChainHandler
    with MultiCallHandler
    with CollectHandler
    with NestedHandler
    with RecursionHandler
    with ErrorHandler {}
