package edu.uci.ics.texera.web.storage

import com.mongodb.client.model.{IndexOptions, Indexes}
import com.mongodb.client.{FindIterable, MongoCollection, MongoCursor}
import org.bson.Document

import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.SeqHasAsJava
import com.mongodb.client.model.Aggregates._
import com.mongodb.client.model.Accumulators._

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.Try

class MongoCollectionManager(collection: MongoCollection[Document]) {

  def insertOne(document: Document): Unit = {
    collection.insertOne(document)
  }

  def insertMany(documents: Iterable[Document]): Unit = {
    collection.insertMany(documents.toSeq.asJava)
  }

  def deleteMany(condition: Document): Unit = {
    collection.deleteMany(condition)
  }

  def getCount: Long = {
    collection.countDocuments()
  }

  def getColumnNames: Array[String] = {
    ???
  }

  def getAllColumnNames: Array[Array[String]] = {
    var result: List[List[String]] = List(List(), List(), List())
    val doc = collection.find().first()
    val keys = doc.keySet()

    keys.forEach { key =>
      val fieldValue = doc.get(key)
      val fieldAsNumber = Try(fieldValue.toString.toDouble).toOption
      fieldAsNumber match {
        case Some(_) => result = result.updated(0, result.head :+ key)
        case None =>
          fieldValue match {
            case _: java.lang.String => result = result.updated(1, result(1) :+ key)
            case _: java.util.Date => result = result.updated(2, result(2) :+ key)
            case _ => {
              println("========================================")
              println(s"Key: $key, Type: ${fieldValue.getClass.getName}")
              println("========================================")
            }
          }
      }
    }
    result.map(_.toArray).toArray
  }

  def getDocuments(condition: Option[Document]): Iterable[Document] = {
    if (condition.isDefined) {
      val cursor: MongoCursor[Document] = collection.find(condition.get).cursor()
      new Iterator[Document] {
        override def hasNext: Boolean = cursor.hasNext
        override def next(): Document = cursor.next()
      }.iterator.to(Iterable)
    } else {
      Iterable(collection.find().first())
    }
  }

  def createIndex(
                   columnName: String,
                   ascendingFlag: Boolean,
                   timeToLiveInMinutes: Option[Int]
                 ): Unit = {
    collection.createIndex(
      Indexes.ascending(columnName),
      new IndexOptions().expireAfter(timeToLiveInMinutes.get, TimeUnit.MINUTES)
    )
  }

  def accessDocuments: FindIterable[Document] = {
    collection.find()
  }

  def calculateNumericStats(fieldName: String, offset: Long): Option[(Any, Any, Any, Long)] = {
    val fieldAsNumber = new Document("$convert", new Document("input", "$" + fieldName).append("to", "double"))
    val projection = new Document(fieldName, fieldAsNumber)
    val pipeline = java.util.Arrays.asList(
      new Document("$skip", offset.toInt),
      new Document("$project", projection),
      new Document("$group", new Document("_id", null)
        .append("minValue", new Document("$min", "$" + fieldName))
        .append("maxValue", new Document("$max", "$" + fieldName))
        .append("meanValue", new Document("$avg", "$" + fieldName))
        .append("count", new Document("$sum", 1))
      )
    )
    val result = collection.aggregate(pipeline).iterator()
    if (result.hasNext) {
      val doc = result.next()
      val count = doc.get("count").toString.toLong
      Some(
        (doc.get("minValue"), doc.get("maxValue"), doc.get("meanValue"), count)  // 返回记录总数
      )
    } else {
      None
    }
  }

  def calculateDateStats(fieldName: String): Option[(Any, Any)] = {
    val fieldAsDate = new Document("$convert", new Document("input", "$" + fieldName).append("to", "date"))
    val projection = new Document(fieldName, fieldAsDate)

    val pipeline = java.util.Arrays.asList(
      new Document("$project", projection),
      new Document("$group", new Document("_id", null)
        .append("minValue", new Document("$min", "$" + fieldName))
        .append("maxValue", new Document("$max", "$" + fieldName))
      )
    )

    val result = collection.aggregate(pipeline).iterator()
    if (result.hasNext) {
      val doc = result.next()
      Some(
        (doc.get("minValue"), doc.get("maxValue"))
      )
    } else {
      None
    }
  }

  /**
   * Calculates statistics for a categorical field including the mode (most common value),
   * the second mode (second most common value), and percentages for these modes.
   * @param fieldName The name of the field for which to calculate the statistics.
   * @return An Option containing a tuple with the mode, second mode, and their percentages, and number of others.
   */
  def calculateCategoricalStats(fieldName: String, offset: Long): mutable.Map[String, Int] = {
    val pipeline = java.util.Arrays.asList(
      new Document("$skip", offset.toInt),
      group("$" + fieldName, java.util.Arrays.asList(
        com.mongodb.client.model.Accumulators.sum("count", 1)
      ))
    )

    val result = collection.aggregate(pipeline).iterator().asScala.toList
    var stats: mutable.Map[String, Int] = mutable.Map()

    result.foreach(doc => {
      stats(doc.getString("_id")) = doc.get("count").asInstanceOf[Number].intValue()
    })

    stats
  }

}
