package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

case class ReplayConfig(fromCheckpoint: Option[Long],
                        replayTo: Option[Long],
                        checkpointConfig: Array[ReplayCheckpointConfig])


case class ReplayCheckpointConfig(recordInputAt: Map[(ActorVirtualIdentity, Boolean), (Long, Long)],
                            checkpointAt: Long)