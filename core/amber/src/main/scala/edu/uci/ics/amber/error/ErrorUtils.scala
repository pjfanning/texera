package edu.uci.ics.amber.error

import com.google.protobuf.timestamp.Timestamp
import edu.uci.ics.amber.engine.architecture.rpc.ConsoleMessage
import edu.uci.ics.amber.engine.architecture.rpc.ConsoleMessageType.ERROR
import edu.uci.ics.amber.engine.architecture.rpc.{ControlException, ErrorLanguage}
import edu.uci.ics.amber.engine.common.VirtualIdentityUtils
import edu.uci.ics.amber.engine.common.ActorVirtualIdentity

import java.time.Instant
import scala.util.control.ControlThrowable

object ErrorUtils {

  /** A helper function for catching all throwable except some special scala internal throwable.
    * reference: https://www.sumologic.com/blog/why-you-should-never-catch-throwable-in-scala/
    * @param handler
    * @tparam T
    * @return
    */
  def safely[T](handler: PartialFunction[Throwable, T]): PartialFunction[Throwable, T] = {
    case ex: ControlThrowable => throw ex
    // case ex: OutOfMemoryError (Assorted other nasty exceptions you don't want to catch)
    // If it's an exception they handle, pass it on
    case ex: Throwable if handler.isDefinedAt(ex) => handler(ex)
    // If they didn't handle it, rethrow automatically
  }

  def mkConsoleMessage(actorId: ActorVirtualIdentity, err: Throwable): ConsoleMessage = {
    val source = if (err.getStackTrace.nonEmpty) {
      "(" + err.getStackTrace.head.getFileName + ":" + err.getStackTrace.head.getLineNumber + ")"
    } else {
      "(Unknown Source)"
    }
    val title = err.toString
    val message = err.getStackTrace.mkString("\n")
    ConsoleMessage(actorId.name, Timestamp(Instant.now), ERROR, source, title, message)
  }

  def mkControlError(err: Throwable): ControlException = {
    val stacktrace = err.getStackTrace.mkString("\n")
    ControlException(err.toString, err.getCause.toString, stacktrace, ErrorLanguage.SCALA)
  }

  def reconstructThrowable(controlError: ControlException): Throwable = {
    if (controlError.language == ErrorLanguage.PYTHON) {
      return new Throwable(controlError.errorMessage)
    } else {
      val reconstructedThrowable = new Throwable(controlError.errorMessage)
      if (controlError.errorDetails.nonEmpty) {
        val causeThrowable = new Throwable(controlError.errorDetails)
        reconstructedThrowable.initCause(causeThrowable)
      }
      val stackTraceElements = controlError.stackTrace.split("\n").map { line =>
        // You need to split each line appropriately to extract the class, method, file, and line number
        val stackTracePattern = """\s*at\s+(.+)\((.+):(\d+)\)""".r
        line match {
          case stackTracePattern(className, fileName, lineNumber) =>
            new StackTraceElement(className, "", fileName, lineNumber.toInt)
          case _ =>
            new StackTraceElement("", "", null, -1) // Handle if stack trace format is invalid
        }
      }
      reconstructedThrowable.setStackTrace(stackTraceElements)
      reconstructedThrowable
    }
  }

  def getStackTraceWithAllCauses(err: Throwable, topLevel: Boolean = true): String = {
    val header = if (topLevel) {
      "Stack trace for developers: \n\n"
    } else {
      "\n\nCaused by:\n"
    }
    val message = header + err.toString + "\n" + err.getStackTrace.mkString("\n")
    if (err.getCause != null) {
      message + getStackTraceWithAllCauses(err.getCause, topLevel = false)
    } else {
      message
    }
  }

  def getOperatorFromActorIdOpt(
      actorIdOpt: Option[ActorVirtualIdentity]
  ): (String, String) = {
    var operatorId = "unknown operator"
    var workerId = ""
    if (actorIdOpt.isDefined) {
      operatorId = VirtualIdentityUtils.getPhysicalOpId(actorIdOpt.get).logicalOpId.id
      workerId = actorIdOpt.get.name
    }
    (operatorId, workerId)
  }

}
