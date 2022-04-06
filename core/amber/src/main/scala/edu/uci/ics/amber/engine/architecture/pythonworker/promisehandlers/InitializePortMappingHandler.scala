package edu.uci.ics.amber.engine.architecture.pythonworker.promisehandlers

import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.LinkIdentity
import edu.uci.ics.texera.workflow.common.tuple.schema.Schema

import scala.collection.mutable

object InitializePortMappingHandler {
  final case class InitializePortMapping(inputToOrdinalMapping: Map[LinkIdentity, (Int, String)], outputToOrdinalMapping: Map[LinkIdentity, (Int, String)])
    extends ControlCommand[Unit]
}
