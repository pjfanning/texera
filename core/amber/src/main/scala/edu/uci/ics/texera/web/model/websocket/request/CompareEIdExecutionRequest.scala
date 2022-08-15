package edu.uci.ics.texera.web.model.websocket.request

import edu.uci.ics.texera.workflow.common.operators.OperatorDescriptor
import edu.uci.ics.texera.workflow.common.workflow.OperatorLink

import scala.collection.mutable

case class CompareEIdExecutionRequest (eId1: Int,
                                       eId2: Int,
                                       operators1: mutable.MutableList[OperatorDescriptor],
                                       operators2: mutable.MutableList[OperatorDescriptor],
                                       links: mutable.MutableList[OperatorLink]) extends TexeraWebSocketRequest
