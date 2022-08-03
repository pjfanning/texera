package edu.uci.ics.texera.web.storage

import com.fasterxml.jackson.databind.node.ObjectNode
import com.mongodb.client.model.{IndexOptions, UpdateOptions}
import com.mongodb.client.{MongoClient, MongoClients, MongoCollection, MongoDatabase}
import edu.uci.ics.amber.engine.common.AmberUtils
import edu.uci.ics.texera.web.model.websocket.event.OperatorStatistics
import org.bson.Document

import java.util
import java.util.Date
import java.util.concurrent.TimeUnit

object MongoStorage {
  val url: String = AmberUtils.amberConfig.getString("storage.mongodb.url")
  val databaseName: String = AmberUtils.amberConfig.getString("storage.mongodb.database")
  val client: MongoClient = MongoClients.create(url)
  val database: MongoDatabase = client.getDatabase(databaseName)
  val timeToLive: Int = AmberUtils.amberConfig.getInt("storage.mongodb.cleanup.ttl-in-seconds")

  def insert(collectionName: String, eId: Int, operator_id: String, opStats: ObjectNode): Unit = {
    var collection: MongoCollection[Document] = null
    val upsertOpt: UpdateOptions = new UpdateOptions().upsert(true)
    if (!collectionExists(collectionName)) {
      database.createCollection(collectionName)
    }
    collection = database.getCollection(collectionName)
    if (operatorExists(collection, operator_id, eId)) {
      collection.updateOne(
        new Document("execution_ID", eId)
          .append("operators.operator_id", operator_id),
        new Document(
          "$set",
          new Document(
            "operators.$",
            Document
              .parse(opStats.toString())
              .append("operator_id", operator_id)
          )
        ),
        upsertOpt
      )
    } else {
      val op: Document = new Document(
        "$push",
        new Document(
          "operators",
          Document
            .parse(opStats.toString)
            .append("operator_id", operator_id)
        )
      )
        .append("$set", new Document("created_at", new Date()))
      collection.updateOne(new Document("execution_ID", eId), op, upsertOpt)
    }
    collection.createIndex(
      new Document("execution_id", eId),
      new IndexOptions().expireAfter(timeToLive, TimeUnit.SECONDS)
    ) //TTL index for expiration in 2 days
  }

  def collectionExists(collectionName: String): Boolean = {
    database
      .listCollectionNames()
      .iterator()
      .forEachRemaining((name: String) => {
        if (name.equals(collectionName)) {
          return true
        }
      })
    false
  }

  def operatorExists(
      collection: MongoCollection[Document],
      operator_id: String,
      eId: Int
  ): Boolean = {
    val op: Document = new Document("execution_ID", eId)
      .append("operators.operator_id", operator_id)
    if (collection.find(op).first() != null) {
      return true
    }
    false
  }

  def getOperatorStat(eId: Int, operator_id: String, collectionName: String): OperatorStatistics = {
    val collection: MongoCollection[Document] = database.getCollection(collectionName)
    val op: Document = new Document("execution_ID", eId)
      .append("operators.operator_id", operator_id)
    var opStatsDoc: Document = collection
      .find(new Document("execution_ID", eId))
      .projection(
        new Document("_id", 0)
          .append("operators", new Document("$elemMatch", new Document("operator_id", operator_id)))
      )
      .first()

    opStatsDoc = opStatsDoc.get("operators").asInstanceOf[util.ArrayList[Document]].get(0)
    OperatorStatistics(
      opStatsDoc.get("state").asInstanceOf[String],
      opStatsDoc.get("inputCount").asInstanceOf[Int].toLong,
      opStatsDoc.get("outputCount").asInstanceOf[Int].toLong
    )

  }
}
