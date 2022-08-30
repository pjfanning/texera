package edu.uci.ics.texera.workflow.common.storage

import com.mongodb.client.{MongoClient, MongoClients, MongoCollection, MongoCursor, MongoDatabase}
import com.mongodb.client.model.Sorts
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.TupleUtils.document2Tuple
import edu.uci.ics.amber.engine.common.AmberUtils
import org.bson.Document

import scala.collection.mutable
import collection.JavaConverters._

class OpResultMongodbStorage(id: String) {
  val url: String = AmberUtils.amberConfig.getString("storage.mongodb.url")
  val databaseName: String = AmberUtils.amberConfig.getString("storage.mongodb.database")
  val client: MongoClient = MongoClients.create(url)
  val database: MongoDatabase = client.getDatabase(databaseName)
  val collection: MongoCollection[Document] = database.getCollection(id)

  // Get the number of result rows from mongodb
  def getCount(): Long = {
    val resultCount = collection.countDocuments()
    resultCount
  }

  // Get the sample row information from mongodb
  def getSampleResultRow(): String = {
    val resultRow = collection.find().first().toJson()
    resultRow
  }
}
