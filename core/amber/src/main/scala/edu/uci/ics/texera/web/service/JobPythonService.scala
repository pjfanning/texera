package edu.uci.ics.texera.web.service

import com.twitter.util.Config.intoList
import com.twitter.util.{Await, Duration}
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{PythonDebugEventTriggered, PythonPrintTriggered}
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.EvaluatePythonExpressionHandler.EvaluatePythonExpression
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.PythonDebugCommandHandler.PythonDebugCommand
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.RetryWorkflowHandler.RetryWorkflow
import edu.uci.ics.amber.engine.common.AmberUtils
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.texera.web.model.websocket.event.TexeraWebSocketEvent
import edu.uci.ics.texera.web.model.websocket.event.python.PythonConsoleUpdateEvent
import edu.uci.ics.texera.web.model.websocket.request.RetryRequest
import edu.uci.ics.texera.web.model.websocket.request.python.{PythonDebugCommandRequest, PythonExpressionEvaluateRequest}
import edu.uci.ics.texera.web.model.websocket.response.python.PythonExpressionEvaluateResponse
import edu.uci.ics.texera.web.service.JobPythonService.bufferSize
import edu.uci.ics.texera.web.storage.JobStateStore
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.{RESUMING, RUNNING}
import edu.uci.ics.texera.web.workflowruntimestate.{EvaluatedValueList, PythonOperatorInfo}
import edu.uci.ics.texera.web.{SubscriptionManager, WebsocketInput}

import scala.collection.mutable

object JobPythonService {
  val bufferSize: Int = AmberUtils.amberConfig.getInt("web-server.python-console-buffer-size")

}

class JobPythonService(
                        client: AmberClient,
                        stateStore: JobStateStore,
                        wsInput: WebsocketInput,
                        breakpointService: JobBreakpointService
                      ) extends SubscriptionManager {
  registerCallbackOnPythonPrint()

  addSubscription(
    stateStore.pythonStore.registerDiffHandler((oldState, newState) => {
      val output = new mutable.ArrayBuffer[TexeraWebSocketEvent]()
      // For each operator, check if it has new python console message or breakpoint events
      newState.operatorInfo
        .foreach {
          case (opId, info) =>
            val oldInfo = oldState.operatorInfo.getOrElse(opId, new PythonOperatorInfo())
            if (info.consoleMessages.nonEmpty) {
              val diff = info.consoleMessages diff oldInfo.consoleMessages
              if (diff.nonEmpty) {
                diff.foreach(s => output.append(PythonConsoleUpdateEvent(s, opId)))
              }
            }
            info.evaluateExprResults.keys.filterNot(oldInfo.evaluateExprResults.contains).foreach {
              key =>
                output.append(
                  PythonExpressionEvaluateResponse(key, info.evaluateExprResults(key).values)
                )
            }
        }
      output
    })
  )

  private[this] def registerCallbackOnPythonPrint(): Unit = {
    addSubscription(
      client
        .registerCallback[PythonPrintTriggered]((evt: PythonPrintTriggered) => {
          stateStore.pythonStore.updateState { jobInfo =>
            val opInfo = jobInfo.operatorInfo.getOrElse(evt.operatorID, PythonOperatorInfo())
            if (opInfo.consoleMessages.size < bufferSize) {
              jobInfo.addOperatorInfo((evt.operatorID, opInfo.addConsoleMessages(evt.message)))
            } else {
              jobInfo.addOperatorInfo(
                (
                  evt.operatorID,
                  opInfo.withConsoleMessages(
                    opInfo.consoleMessages.drop(1) :+ evt.message
                  )
                )
              )
            }
          }
        })
    )
  }

  private[this] def registerCallbackOnPythonDebugEvent(): Unit = {
    addSubscription(
      client
        .registerCallback[PythonDebugEventTriggered]((evt: PythonDebugEventTriggered) => {
          stateStore.pythonStore.updateState { jobInfo =>
            val opInfo = jobInfo.operatorInfo.getOrElse(evt.operatorId, PythonOperatorInfo())
            if (opInfo.consoleMessages.size < bufferSize) {
              jobInfo.addOperatorInfo((evt.operatorId, opInfo.addConsoleMessages(evt.message)))
            } else {
              jobInfo.addOperatorInfo(
                (
                  evt.operatorId,
                  opInfo.withConsoleMessages(
                    opInfo.consoleMessages.drop(1) :+ evt.message
                  )
                )
              )
            }
          }
        })
    )
  }

  //Receive retry request
  addSubscription(wsInput.subscribe((req: RetryRequest, uidOpt) => {
    breakpointService.clearTriggeredBreakpoints()
    stateStore.jobMetadataStore.updateState(jobInfo => jobInfo.withState(RESUMING))
    client.sendAsyncWithCallback[Unit](
      RetryWorkflow(),
      _ => stateStore.jobMetadataStore.updateState(jobInfo => jobInfo.withState(RUNNING))
    )
  }))

  //Receive debug command
  addSubscription(wsInput.subscribe((req: PythonDebugCommandRequest, uidOpt) => {
    stateStore.pythonStore.updateState { jobInfo =>
      val opInfo = jobInfo.operatorInfo.getOrElse(req.operatorId, PythonOperatorInfo())
      if (opInfo.consoleMessages.size < bufferSize) {
        jobInfo.addOperatorInfo((req.operatorId, opInfo.addConsoleMessages(req.cmd)))
      } else {
        jobInfo.addOperatorInfo(
          (
            req.operatorId,
            opInfo.withConsoleMessages(
              opInfo.consoleMessages.drop(1) :+ req.cmd
            )
          )
        )
      }
    }

    client.sendAsync(PythonDebugCommand(req.operatorId, req.workerId, req.cmd))

  }))

  //Receive evaluate python expression
  addSubscription(wsInput.subscribe((req: PythonExpressionEvaluateRequest, uidOpt) => {
    val result = Await.result(
      client.sendAsync(EvaluatePythonExpression(req.expression, req.operatorId)),
      Duration.fromSeconds(10)
    )
    stateStore.pythonStore.updateState(pythonStore => {
      val opInfo = pythonStore.operatorInfo.getOrElse(req.operatorId, PythonOperatorInfo())
      pythonStore.addOperatorInfo(
        (
          req.operatorId,
          opInfo.addEvaluateExprResults((req.expression, EvaluatedValueList(result)))
        )
      )
    })
    // TODO: remove the following hack after fixing the frontend
    // currently frontend is not prepared for re-receiving the eval-expr messages
    // so we add it to the state and remove it from the state immediately
    stateStore.pythonStore.updateState(pythonStore => {
      val opInfo = pythonStore.operatorInfo.getOrElse(req.operatorId, PythonOperatorInfo())
      pythonStore.addOperatorInfo((req.operatorId, opInfo.clearEvaluateExprResults))
    })
  }))

}
