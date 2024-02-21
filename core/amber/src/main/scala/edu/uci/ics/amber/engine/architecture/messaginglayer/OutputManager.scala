package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.architecture.messaginglayer.OutputManager.{
  getBatchSize,
  toPartitioner
}
import edu.uci.ics.amber.engine.architecture.sendsemantics.partitioners._
import edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings._
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.tuple.amber.{MapTupleLike, SeqTupleLike, TupleLike}
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, ChannelIdentity}
import edu.uci.ics.amber.engine.common.workflow.PhysicalLink
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.Schema
import org.jooq.exception.MappingException

import scala.collection.mutable
import scala.jdk.CollectionConverters.CollectionHasAsScala

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

  val partitioners = mutable.HashMap[PhysicalLink, Partitioner]()

  val networkOutputBuffers =
    mutable.HashMap[(PhysicalLink, ActorVirtualIdentity), NetworkOutputBuffer]()

  /**
    * Add down stream operator and its corresponding Partitioner.
    * @param partitioning Partitioning, describes how and whom to send to.
    */
  def addPartitionerWithPartitioning(
      link: PhysicalLink,
      partitioning: Partitioning
  ): Unit = {
    val partitioner = toPartitioner(partitioning)
    partitioners.update(link, partitioner)
    partitioner.allReceivers.foreach(receiver => {
      val buffer = new NetworkOutputBuffer(receiver, dataOutputPort, getBatchSize(partitioning))
      networkOutputBuffers.update((link, receiver), buffer)
      dataOutputPort.addOutputChannel(ChannelIdentity(selfID, receiver, isControl = false))
    })
  }

  /**
    * Push one tuple to the downstream, will be batched by each transfer partitioning.
    * Should ONLY be called by DataProcessor.
    * @param tupleLike TupleLike to be passed.
    */
  def passTupleToDownstream(
      tupleLike: TupleLike,
      outputLink: PhysicalLink,
      schema: Schema
  ): Unit = {
    val partitioner =
      partitioners.getOrElse(outputLink, throw new MappingException("output port not found"))
    val outputTuple: ITuple = if (schema != null) {
      enforceSchema(tupleLike, schema)
    } else {
      ITuple(tupleLike)
    }
    val it = partitioner.getBucketIndex(outputTuple)
    it.foreach(bucketIndex => {
      val destActor = partitioner.allReceivers(bucketIndex)
      networkOutputBuffers((outputLink, destActor)).addTuple(outputTuple)
    })
  }

  /**
    * Transforms a TupleLike object to a Tuple that conforms to a given Schema.
    *
    * @param tupleLike The TupleLike object to be transformed.
    * @param schema The Schema to which the tupleLike object must conform.
    * @return A Tuple that matches the specified schema.
    * @throws RuntimeException if the tupleLike object type is unsupported or invalid for schema enforcement.
    */
  def enforceSchema(tupleLike: TupleLike, schema: Schema): Tuple = {
    tupleLike match {
      case tTuple: Tuple =>
        assert(
          tTuple.getSchema == schema,
          s"output tuple schema does not match the expected schema! " +
            s"output schema: ${tTuple.getSchema}, " +
            s"expected schema: $schema"
        )
        tTuple
      case map: MapTupleLike =>
        val seq = reorderFields(map.fieldMappings, schema)
        buildTupleWithSchema(seq, schema)
      case seq: SeqTupleLike =>
        buildTupleWithSchema(seq.fieldArray, schema)
      case iTuple: ITuple =>
        buildTupleWithSchema(iTuple.toSeq, schema)
      case _ => throw new RuntimeException("invalid tuple type, cannot enforce schema")
    }
  }

  private def reorderFields(fieldMappings: Map[String, Any], schema: Schema): Seq[Any] = {
    val result = Array.fill[Any](schema.getAttributes.size())(null)
    fieldMappings.foreach {
      case (name, value) =>
        result(schema.getIndex(name)) = value
    }
    result
  }

  private def buildTupleWithSchema(fields: Seq[Any], schema: Schema): Tuple = {
    assert(
      fields.size == schema.getAttributes.size,
      s"the size of the output: ${fields.mkString(",")} does not match to the size of schema"
    )
    val attributes = schema.getAttributes.asScala
    val builder = Tuple.newBuilder(schema)
    attributes.zipWithIndex.foreach {
      case (attr, i) =>
        builder.add(attr, fields(i))
    }
    builder.build()
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
            val channel = ChannelIdentity(selfID, out._1._2, isControl = false)
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
