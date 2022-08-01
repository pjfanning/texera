package edu.uci.ics.texera.web.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema.Items
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.{MongoClient, MongoClients, MongoCollection, MongoDatabase, MongoIterable}
import edu.uci.ics.amber.engine.common.AmberUtils
import org.bson.Document

import java.util.ArrayList



object OPMongoStorage {
  val url: String = AmberUtils.amberConfig.getString("storage.mongodb.url")
  val databaseName: String = AmberUtils.amberConfig.getString("storage.mongodb.database")
  val client: MongoClient = MongoClients.create(url)
  val database: MongoDatabase = client.getDatabase(databaseName)

  def insert(collectionName: String, eId: Int, operator_id: String,opStats: ObjectNode): Unit = {
    if (!collectionExists(collectionName)) {
      database.createCollection(collectionName)
      val collection: MongoCollection[Document] = database.getCollection(collectionName)
      val singleOP: Document = Document.parse(opStats.toString).append("operator_id", operator_id)
      val opList: ArrayList[Document] = new ArrayList[Document]()
      opList.add(singleOP)
      val op:Document = new Document("execution_ID", eId.toString).append("operators",opList).append("expiredAfterSeconds", 178000)
      collection.insertOne(op)
    } else {
      if (!operatorExists(collectionName, operator_id, eId)){ //to eliminate extra requests
        val options: UpdateOptions = new UpdateOptions().upsert(true)
        val collection: MongoCollection[Document] = database.getCollection(collectionName)
        val op: Document = new Document("$push", new Document("operators", Document.parse(opStats.toString).append("operator_id", operator_id)))
        collection.updateOne(new Document("execution_ID", eId.toString), op, options)
      }
    }
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
