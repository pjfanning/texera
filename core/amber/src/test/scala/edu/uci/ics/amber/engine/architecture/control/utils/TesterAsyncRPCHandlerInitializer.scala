package edu.uci.ics.amber.engine.architecture.control.utils

import edu.uci.ics.amber.engine.architecture.worker.processing.AmberProcessor
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCHandlerInitializer

class TesterAsyncRPCHandlerInitializer(val processor: AmberProcessor)
    extends AsyncRPCHandlerInitializer(processor.asyncRPCClient, processor.asyncRPCServer)
    with PingPongHandler
    with ChainHandler
    with MultiCallHandler
    with CollectHandler
    with NestedHandler
    with RecursionHandler
    with ErrorHandler {}
