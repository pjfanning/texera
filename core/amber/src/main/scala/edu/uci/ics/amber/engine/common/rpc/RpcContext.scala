package edu.uci.ics.amber.engine.common.rpc

import edu.uci.ics.amber.engine.common.ambermessage.ChannelEndpointID

class RpcContext {
  var receivingChannel:ChannelEndpointID = _
  var enabledTracing:Boolean = false
}
