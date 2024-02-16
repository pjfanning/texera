package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.architecture.messaginglayer.OutputManager.{
  getBatchSize,
  toPartitioner
}
import edu.uci.ics.amber.engine.architecture.sendsemantics.partitioners._
import edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings._
import edu.uci.ics.amber.engine.architecture.worker.DataProcessor
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.tuple.amber.{MapTupleLike, SeqTupleLike, TupleLike}
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, ChannelIdentity}
import edu.uci.ics.amber.engine.common.workflow.{PhysicalLink, PortIdentity}
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.Schema
import org.jooq.exception.MappingException

import scala.collection.mutable

object OutputManager {

  final case class FlushNetworkBuffer() extends ControlCommand[Unit]

  // create a corresponding partitioner for the given partitioning policy
  def toPartitioner(partitioning: Partitioning): Partitioner = {
    val partitioner = partitioning match {
      case oneToOnePartitioning: OneToOnePartitioning => OneToOnePartitioner(oneToOnePartitioning)
      case roundRobinPartitioning: RoundRobinPartitioning =>
        RoundRobinPartitioner(roundRobinPartitioning)
      case hashBasedShufflePartitioning: HashBasedShufflePartitioning =>
        HashBasedShufflePartitioner(hashBasedShufflePartitioning)
      case rangeBasedShufflePartitioning: RangeBasedShufflePartitioning =>
        RangeBasedShufflePartitioner(rangeBasedShufflePartitioning)
      case broadcastPartitioning: BroadcastPartitioning =>
        BroadcastPartitioner(broadcastPartitioning)
      case _ => throw new RuntimeException(s"partitioning $partitioning not supported")
    }
    partitioner
  }

  def getBatchSize(partitioning: Partitioning): Int = {
    partitioning match {
      case p: OneToOnePartitioning          => p.batchSize
      case p: RoundRobinPartitioning        => p.batchSize
      case p: HashBasedShufflePartitioning  => p.batchSize
      case p: RangeBasedShufflePartitioning => p.batchSize
      case p: BroadcastPartitioning         => p.batchSize
      case _                                => throw new RuntimeException(s"partitioning $partitioning not supported")
    }
  }
}

/** This class is a container of all the transfer partitioners.
  *
  * @param selfID         ActorVirtualIdentity of self.
  * @param dataOutputPort DataOutputPort
  */
class OutputManager(
    selfID: ActorVirtualIdentity,
    dataOutputPort: NetworkOutputGateway
) {

  val partitioners = mutable.HashMap[PortIdentity, (Partitioner, Schema)]()

  val networkOutputBuffers =
    mutable.HashMap[ActorVirtualIdentity, NetworkOutputBuffer]()

  /**
    * Add down stream operator and its corresponding Partitioner.
    * @param partitioning Partitioning, describes how and whom to send to.
    */
  def addPartitionerWithPartitioning(
      portId: PortIdentity,
      partitioning: Partitioning,
      schema: Schema
  ): Unit = {
    val partitioner = toPartitioner(partitioning)
    partitioners.update(portId, (partitioner, schema))
    partitioner.allReceivers.foreach(receiver => {
      val buffer = new NetworkOutputBuffer(receiver, dataOutputPort, getBatchSize(partitioning))
      networkOutputBuffers.update(receiver, buffer)
      dataOutputPort.addOutputChannel(ChannelIdentity(selfID, receiver, isControl = false))
    })
  }

  /**
    * Push one tuple to the downstream, will be batched by each transfer partitioning.
    * Should ONLY be called by DataProcessor.
    * @param tupleLike ITuple to be passed.
    */
  def passTupleToDownstream(
      tupleLike: TupleLike,
      outputPort: PortIdentity
  ): Unit = {
    val (partitioner, schema) =
      partitioners.getOrElse(outputPort, throw new MappingException("output port not found"))
    val outputTuple: Tuple = tupleLike match {
      case tTuple: Tuple => {
        assert(tTuple.getSchema == schema)
        tTuple
      }
      case map: MapTupleLike => {
        val builder = Tuple.newBuilder(schema)
        map.fieldMappings.foreach {
          case (str, value) =>
            builder.add(schema.getAttribute(str), value)
        }
        builder.build()
      }
      case seq: SeqTupleLike => {
        val builder = Tuple.newBuilder(schema)
        val fieldAttrs = schema.getAttributes
        seq.fieldArray.indices.foreach { i =>
          builder.add(fieldAttrs.get(i), seq.fieldArray(i))
        }
        builder.build()
      }
      case _ => throw new RuntimeException("invalid tuple type, cannot enforce schema")
    }
    val it = partitioner.getBucketIndex(outputTuple)
    it.foreach(bucketIndex => {
      val destActor = partitioner.allReceivers(bucketIndex)
      networkOutputBuffers(destActor).addTuple(outputTuple)
    })
  }

  /**
    * Flushes the network output buffers based on the specified set of physical links.
    *
    * This method flushes the buffers associated with the network output. If the 'onlyFor' parameter
    * is specified with a set of 'PhysicalLink's, only the buffers corresponding to those links are flushed.
    * If 'onlyFor' is None, all network output buffers are flushed.
    *
    * @param onlyFor An optional set of 'ChannelID' indicating the specific buffers to flush.
    *                If None, all buffers are flushed. Default value is None.
    */
  def flush(onlyFor: Option[Set[ChannelIdentity]] = None): Unit = {
    val buffersToFlush = onlyFor match {
      case Some(channelIds) =>
        networkOutputBuffers
          .filter(out => {
            val channel = ChannelIdentity(selfID, out._1, isControl = false)
            channelIds.contains(channel)
          })
          .values
      case None => networkOutputBuffers.values
    }
    buffersToFlush.foreach(_.flush())
  }

  /**
    * Send the last batch and EOU marker to all down streams
    */
  def emitEndOfUpstream(): Unit = {
    // flush all network buffers of this operator, emit end marker to network
    networkOutputBuffers.foreach(kv => {
      kv._2.flush()
      kv._2.noMore()
    })
  }

}
