package edu.uci.ics.texera.workflow.operators.sink.storage

import com.mongodb.client.model.Sorts
import com.mongodb.client.MongoCursor
import edu.uci.ics.amber.engine.common.AmberConfig
import edu.uci.ics.texera.web.storage.{MongoCollectionManager, MongoDatabaseManager}
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.TupleUtils.document2Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.Schema
import org.bson.Document

import scala.collection.mutable

class MongoDBSinkStorage(id: String) extends SinkStorageReader {

  val commitBatchSize: Int = AmberConfig.sinkStorageMongoDBConfig.getInt("commit-batch-size")
  MongoDatabaseManager.dropCollection(id)
  @transient lazy val collectionMgr: MongoCollectionManager = MongoDatabaseManager.getCollection(id)

  var previousCount: Long = 0
  var previousNumStats: mutable.Map[String, (Double, Double, Double)] = mutable.Map()
  var previousDateStats: mutable.Map[String, (java.util.Date, java.util.Date)] = mutable.Map()

  class MongoDBSinkStorageWriter(bufferSize: Int) extends SinkStorageWriter {
    var uncommittedInsertions: mutable.ArrayBuffer[Tuple] = _
    @transient lazy val collection: MongoCollectionManager = MongoDatabaseManager.getCollection(id)

    override def open(): Unit = {
      uncommittedInsertions = new mutable.ArrayBuffer[Tuple]()
    }

    override def close(): Unit = {
      if (uncommittedInsertions.nonEmpty) {
        collection.insertMany(uncommittedInsertions.map(_.asDocument()))
        uncommittedInsertions.clear()
      }
    }

    override def putOne(tuple: Tuple): Unit = {
      uncommittedInsertions.append(tuple)
      if (uncommittedInsertions.size == bufferSize) {
        collection.insertMany(uncommittedInsertions.map(_.asDocument()))
        uncommittedInsertions.clear()
      }
    }

    override def removeOne(tuple: Tuple): Unit = {
      val index = uncommittedInsertions.indexOf(tuple)
      if (index != -1) {
        uncommittedInsertions.remove(index)
      } else {
        collection.deleteMany(tuple.asDocument())
      }
    }
  }

  private[this] def mkTupleIterable(cursor: MongoCursor[Document]): Iterable[Tuple] = {
    new Iterator[Tuple] {
      override def hasNext: Boolean = cursor.hasNext
      override def next(): Tuple = document2Tuple(cursor.next(), schema)
    }.iterator.to(Iterable)
  }

  override def getAll: Iterable[Tuple] = {
    val cursor = collectionMgr.accessDocuments.sort(Sorts.ascending("_id")).cursor()
    mkTupleIterable(cursor)
  }

  override def getStorageWriter: SinkStorageWriter =
    new MongoDBSinkStorageWriter(commitBatchSize)

  override def clear(): Unit = {
    MongoDatabaseManager.dropCollection(id)
  }

  override def getRange(from: Int, to: Int): Iterable[Tuple] = {
    val cursor =
      collectionMgr.accessDocuments
        .sort(Sorts.ascending("_id"))
        .limit(to - from)
        .skip(from)
        .cursor()
    mkTupleIterable(cursor)
  }

  override def getCount: Long = {
    collectionMgr.getCount
  }

  override def getAllAfter(offset: Int): Iterable[Tuple] = {
    val cursor = collectionMgr.accessDocuments.sort(Sorts.ascending("_id")).skip(offset).cursor()
    mkTupleIterable(cursor)
  }

  override def getSchema: Schema = {
    synchronized {
      schema
    }
  }

  override def setSchema(schema: Schema): Unit = {
    // Now we require mongodb version > 5 to support "." in field names
    synchronized {
      this.schema = schema
    }
  }

  override def getAllFields(): Array[Array[String]] = {
    collectionMgr.getAllColumnNames
  }

  override def getNumericColStats(fields: Iterable[String]): Map[String, Map[String, Any]] = {
    var result = Map[String, Map[String, Any]]()
//    val currentCount = collectionMgr.getCount

    fields.foreach(field => {
      var fieldResult = Map[String, Any]()
      val stats = collectionMgr.calculateNumericStats(field, previousCount)

      stats match {
        case Some((minValue, maxValue, meanValue, newCount)) =>
          val (prevMin, prevMax, prevMean) = previousNumStats.getOrElse(field, (Double.MaxValue, Double.MinValue, 0.0))

          val newMin = if (minValue != null) Math.min(prevMin, minValue.toString.toDouble) else prevMin
          val newMax = if (maxValue != null) Math.max(prevMax, maxValue.toString.toDouble) else prevMax
          val newMean = if (meanValue != null) (prevMean * previousCount + meanValue.toString.toDouble * newCount) / (previousCount + newCount) else prevMean

          previousNumStats(field) = (newMin, newMax, newMean)

          fieldResult += ("min" -> newMin)
          fieldResult += ("max" -> newMax)
          fieldResult += ("mean" -> newMean)
        case _ =>
          val (prevMin, prevMax, prevMean) = previousNumStats.getOrElse(field, (Double.MaxValue, Double.MinValue, 0.0))
          val newMin = prevMin
          val newMax = prevMax
          val newMean = prevMean
          fieldResult += ("min" -> newMin)
          fieldResult += ("max" -> newMax)
          fieldResult += ("mean" -> newMean)
      }

      if (fieldResult.nonEmpty) result += (field -> fieldResult)
    })

//    previousCount = currentCount
    result
  }

  override def updatePreviousCount(): Unit = {
    previousCount = collectionMgr.getCount
  }

  override def getDateColStats(fields: Iterable[String]): Map[String, Map[String, Any]] = {
    var result = Map[String, Map[String, Any]]()

    fields.foreach(field => {
      var fieldResult = Map[String, Any]()
      val stats = collectionMgr.calculateDateStats(field, previousCount)

      stats match {
        case Some((minValue: java.util.Date, maxValue: java.util.Date)) =>
          val (prevMin, prevMax) = previousDateStats.getOrElse(field, (new java.util.Date(Long.MaxValue), new java.util.Date(Long.MinValue)))

          val newMin = if (minValue != null && minValue.before(prevMin)) minValue else prevMin
          val newMax = if (maxValue != null && maxValue.after(prevMax)) maxValue else prevMax

          previousDateStats(field) = (newMin, newMax)

          fieldResult += ("min" -> newMin)
          fieldResult += ("max" -> newMax)

        case _ =>
          val (prevMin, prevMax) = previousDateStats.getOrElse(field, (new java.util.Date(Long.MaxValue), new java.util.Date(Long.MinValue)))
          fieldResult += ("min" -> prevMin)
          fieldResult += ("max" -> prevMax)
      }

      if (fieldResult.nonEmpty) result += (field -> fieldResult)
    })
    result
  }

  override def getCatColStats(fields: Iterable[String]): Map[String, Map[String, Any]] = {
    var result = Map[String, Map[String, Any]]()

    fields.foreach(field => {
      var fieldResult = Map[String, Any]()
      val stats = collectionMgr.calculateCategoricalStats(field)

      stats match {
        case Some((mode1, mode2, percent1, percent2, other)) => {
          fieldResult = fieldResult + (
            "firstCat" -> mode1,
            "secondCat" -> mode2,
            "firstPercent" -> percent1,
            "secondPercent" -> percent2,
            "other" -> other
          )
        }
        case None => None
      }

      if (fieldResult.nonEmpty) result = result + (field -> fieldResult)
    })

    result
  }
}

