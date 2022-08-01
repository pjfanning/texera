package edu.uci.ics.texera.web.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.mongodb.client.model.{IndexOptions, UpdateOptions}
import com.mongodb.client.{MongoClient, MongoClients, MongoCollection, MongoDatabase}
import edu.uci.ics.amber.engine.common.AmberUtils
import org.bson.Document
import java.util.concurrent.TimeUnit
import java.util.Date

object OPMongoStorage {
  val url: String = AmberUtils.amberConfig.getString("storage.mongodb.url")
  val databaseName: String = AmberUtils.amberConfig.getString("storage.mongodb.database")
  val client: MongoClient = MongoClients.create(url)
  val database: MongoDatabase = client.getDatabase(databaseName)

  def insert(collectionName: String, eId: Int, operator_id: String,opStats: ObjectNode): Unit = {
    var collection: MongoCollection[Document] = null
    val upsertOpt: UpdateOptions = new UpdateOptions().upsert(true)

    if (!collectionExists(collectionName)) {
      database.createCollection(collectionName)
    }
    collection = database.getCollection(collectionName)
    if (!operatorExists(collectionName, operator_id, eId)){ //to avoid redundant events
      val op: Document = new Document("$push", new Document("operators", Document.parse(opStats.toString).append("operator_id", operator_id))).append("$set", new Document("created_at", new Date()))
      collection.updateOne(new Document("execution_ID", eId), op, upsertOpt)
    }
    collection.createIndex(new Document("execution_id", eId), new IndexOptions().expireAfter(172800, TimeUnit.SECONDS)) //TTL index for expiration in 2 days
  }

  def collectionExists(collectionName: String): Boolean = {
    database.listCollectionNames().iterator().forEachRemaining(
      (name: String) => {
        if (name.equals(collectionName)) {
          return true
        }
      }
    )
    false
  }

  def operatorExists(collectionName:String, operator_id: String, eId: Int): Boolean = {
    val collection: MongoCollection[Document] = database.getCollection(collectionName)
    val op: Document = new Document("execution_ID", eId.toString).append("operators.operator_id", operator_id)
    if (collection.find(op).first() != null) {
      return true
    }
    false
  }
}
