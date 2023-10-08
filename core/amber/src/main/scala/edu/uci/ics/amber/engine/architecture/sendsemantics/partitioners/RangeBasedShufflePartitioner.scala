package edu.uci.ics.amber.engine.architecture.sendsemantics.partitioners

import edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.RangeBasedShufflePartitioning
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.AttributeType

import java.sql.Timestamp

case class RangeBasedShufflePartitioner(partitioning: RangeBasedShufflePartitioning)
    extends Partitioner {

  override def getBucketIndex(tuple: ITuple): Int = {
    // Do range partitioning only on the first attribute in `rangeColumnIndices`.
    val fieldType = tuple
      .asInstanceOf[Tuple]
      .getSchema
      .getAttributes()
      .get(partitioning.rangeColumnIndices(0))
      .getType
    var fieldVal: Long = -1
    fieldType match {
      case AttributeType.LONG =>
        fieldVal = tuple.getLong(partitioning.rangeColumnIndices(0))
      case AttributeType.INTEGER =>
        fieldVal = tuple.getInt(partitioning.rangeColumnIndices(0)).toLong
      case AttributeType.DOUBLE =>
        fieldVal = tuple.getDouble(partitioning.rangeColumnIndices(0)).toLong
      case AttributeType.TIMESTAMP =>
        fieldVal = tuple.get(partitioning.rangeColumnIndices(0)).asInstanceOf[Timestamp].getTime
      case _ =>
        throw new RuntimeException("unsupported attribute type: " + fieldType.toString())
    }
    val keysPerReceiver = (Long.MaxValue / partitioning.receivers.length)
    ((fieldVal) / keysPerReceiver).toInt
  }

  override def allReceivers: Seq[ActorVirtualIdentity] = partitioning.receivers

}
