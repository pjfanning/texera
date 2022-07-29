package edu.uci.ics.texera.web.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ObjectMapper
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{WorkflowCompleted, WorkflowStatusUpdate}
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.texera.Utils
import edu.uci.ics.texera.web.SubscriptionManager
import edu.uci.ics.texera.web.model.websocket.event.{OperatorStatistics, OperatorStatisticsUpdateEvent, TexeraWebSocketEvent}
import edu.uci.ics.texera.web.storage.{JobStateStore, WorkflowStateStore}
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.{ABORTED, COMPLETED}

class JobStatsService(
    client: AmberClient,
    stateStore: JobStateStore
) extends SubscriptionManager {

  registerCallbacks()

  addSubscription(
    stateStore.statsStore.registerDiffHandler((oldState, newState) => {
      // Update operator stats if any operator updates its stat
      if (newState.operatorInfo.toSet != oldState.operatorInfo.toSet) {
        Iterable(
          OperatorStatisticsUpdateEvent(newState.operatorInfo.collect {
            case x =>
              val stats = x._2
              val res = OperatorStatistics(
                Utils.aggregatedStateToString(stats.state),
                stats.inputCount,
                stats.outputCount
              )

              //only proceed if stats.state is COMPLETED/ABORTED?
              var mapper: ObjectMapper = new ObjectMapper()
              //TODO: these two belong somewhere else so a new one doesn't get created every time, specific to an execution
              var execution: ObjectNode = mapper.createObjectNode() //TODO: obtain the eId here? or let mongo manager obtain it?
              var operator: ObjectNode = mapper.createObjectNode()

              var opStats:ObjectNode  = mapper.createObjectNode() //mongo manager will need to check if operator ID already exists in the array


              opStats.put("state", Utils.aggregatedStateToString(stats.state))
              opStats.put("inputCount", stats.inputCount)
              opStats.put("outputCount", stats.outputCount)

              operator.set(x._1, opStats)

//              execution.set("operators", ) //need to build an ArrayNode to store operators
//              execution.put("expiredAfterSeconds", 172800)

              println("------------------")
              println(operator.toString)
              println("------------------")
              (x._1, res)
          })
        )
      } else {
        Iterable.empty
      }
    })
  )

  private[this] def registerCallbacks(): Unit = {
    registerCallbackOnWorkflowStatusUpdate()
    registerCallbackOnWorkflowComplete()
    registerCallbackOnFatalError()
  }

  private[this] def registerCallbackOnWorkflowStatusUpdate(): Unit = {
    addSubscription(
      client
        .registerCallback[WorkflowStatusUpdate]((evt: WorkflowStatusUpdate) => {
          stateStore.statsStore.updateState { jobInfo =>
            jobInfo.withOperatorInfo(evt.operatorStatistics)
          }
        })
    )
  }

  private[this] def registerCallbackOnWorkflowComplete(): Unit = {
    addSubscription(
      client
        .registerCallback[WorkflowCompleted]((evt: WorkflowCompleted) => {
          client.shutdown()
          stateStore.jobMetadataStore.updateState(jobInfo => jobInfo.withState(COMPLETED))
        })
    )
  }

  private[this] def registerCallbackOnFatalError(): Unit = {
    addSubscription(
      client
        .registerCallback[FatalError]((evt: FatalError) => {
          client.shutdown()
          stateStore.jobMetadataStore.updateState { jobInfo =>
            jobInfo.withState(ABORTED).withError(evt.e.getLocalizedMessage)
          }
        })
    )
  }
}
