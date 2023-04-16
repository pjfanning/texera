package edu.uci.ics.amber.engine.architecture.control.utils

import edu.uci.ics.amber.engine.architecture.worker.processing.AmberProcessor
import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCHandlerInitializer, AsyncRPCServer}

class TesterAsyncRPCHandlerInitializer(val processor: AmberProcessor)
    extends AsyncRPCHandlerInitializer(processor.asyncRPCClient, processor.asyncRPCServer)
    with PingPongHandler
    with ChainHandler
    with MultiCallHandler
    with CollectHandler
    with NestedHandler
    with RecursionHandler
    with ErrorHandler {}
