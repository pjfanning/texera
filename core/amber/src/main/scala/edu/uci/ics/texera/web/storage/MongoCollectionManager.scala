package edu.uci.ics.texera.web.storage

import com.mongodb.client.model.{IndexOptions, Indexes}
import com.mongodb.client.{FindIterable, MongoCollection, MongoCursor}
import org.bson.Document

import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.SeqHasAsJava

import com.mongodb.client.model.Aggregates._
import com.mongodb.client.model.Accumulators._

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

  def getNumericColumnNames: Array[String] = {
    var result = List[String]()
    val doc = collection.find().first()
    val keys = doc.keySet()

    keys.forEach { key =>
      val fieldValue = doc.get(key)
      fieldValue match {
        case number: java.lang.Number => result = result :+ key
        case _ => result
      }
    }

    result.toArray
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

  def calculateStats(fieldName: String): Option[(Any, Any, Any)] = {
    val pipeline = java.util.Arrays.asList(
      group(null,
        min("minValue", "$" + fieldName),
        max("maxDomain", "$" + fieldName),
        avg("meanValue", "$" + fieldName))
    )

    val result = collection.aggregate(pipeline)

    if (result.iterator().hasNext()) {
      val doc = result.iterator().next()
      Option(
        doc.get("minValue"),
        doc.get("maxValue"),
        doc.get("meanValue")
      )
    } else {
      None
    }
  }
}
