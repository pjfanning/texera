package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.common.model.tuple.Schema
import edu.uci.ics.amber.engine.common.ChannelIdentity

import scala.collection.mutable

case class WorkerPort(
    schema: Schema,
    // TODO: change it to manage the actual AmberFIFOChannel instead of Boolean
    channels: mutable.HashMap[ChannelIdentity, Boolean] = mutable.HashMap()
)
