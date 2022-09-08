package edu.uci.ics.texera.web.model.websocket.request

case class ComparisonResultPaginationRequest (
    requestID: String,
    operatorID: String,
    pageIndex: Int,
    pageSize: Int,
    isTopWorkflow: Boolean
) extends TexeraWebSocketRequest

