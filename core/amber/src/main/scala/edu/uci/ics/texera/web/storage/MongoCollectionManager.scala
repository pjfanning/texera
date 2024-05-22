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

  def calculateMin(fieldName: String): Option[Any] = {
    val pipeline = java.util.Arrays.asList(
      group(null, min("minValue", "$" + fieldName))
    )

    // 这里使用Java驱动的API来执行聚合查询
    val result = collection.aggregate(pipeline)

    // 使用Java的迭代器来遍历结果
    if (result.iterator().hasNext()) {
      val doc = result.iterator().next()
      Option(doc.get("minValue"))
    } else {
      None
    }
  }
}
