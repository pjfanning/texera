package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.common.ambermessage.ChannelEndpointID
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

case class ReplayConfig(fromCheckpoint: Option[Long],
                        replayTo: Option[Long],
                        checkpointConfig: Array[ReplayCheckpointConfig])


case class ReplayCheckpointConfig(recordInputAt: Map[ChannelEndpointID, (Long, Long)],
                            checkpointAt: Long)