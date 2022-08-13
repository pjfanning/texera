package edu.uci.ics.texera.web.model.websocket.request

import edu.uci.ics.texera.workflow.common.operators.OperatorDescriptor
import edu.uci.ics.texera.workflow.common.workflow.{BreakpointInfo, OperatorLink}

import scala.collection.mutable

case class RegisterEIdRequest (eId: Int,
                               operators: mutable.MutableList[OperatorDescriptor],
                               links: mutable.MutableList[OperatorLink],
                               comparisonFlag: Boolean) extends TexeraWebSocketRequest
