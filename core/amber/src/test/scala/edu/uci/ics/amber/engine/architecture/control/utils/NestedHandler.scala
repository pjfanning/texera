package edu.uci.ics.amber.engine.architecture.control.utils

import com.twitter.util.Promise
import edu.uci.ics.amber.engine.architecture.control.utils.NestedHandler.{Nested, Pass}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object NestedHandler {
  case class Nested(k: Int) extends ControlCommand[String]

  case class Pass(value: String) extends ControlCommand[String]
}

trait NestedHandler {
  this: TrivialControlTester =>

  registerHandler { (n: Nested, sender) =>
    send(Pass("Hello"), actorId)
      .flatMap(ret => send(Pass(ret + " "), actorId))
      .flatMap(ret => send(Pass(ret + "World!"), actorId))
  }

  registerHandler { (p: Pass, sender) =>
    p.value
  }
}
