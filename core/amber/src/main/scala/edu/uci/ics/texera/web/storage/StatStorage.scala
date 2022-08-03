package edu.uci.ics.texera.web.storage

import com.mongodb.BasicDBObject
import com.mongodb.client.model.{IndexOptions, Indexes}
import com.mongodb.client.{MongoClient, MongoClients, MongoCollection, MongoDatabase}
import edu.uci.ics.amber.engine.common.AmberUtils
import edu.uci.ics.texera.web.TexeraWebApplication
import edu.uci.ics.texera.web.model.websocket.event.OperatorStatistics
import org.bson.Document

import java.util.Date
import java.util.concurrent.TimeUnit
import scala.collection.mutable
import scala.concurrent.duration.DurationInt

object StatStorage {
  var uncommittedStats: mutable.Map[Int, mutable.Map[String, OperatorStatistics]] = mutable.Map()

  val url: String = AmberUtils.amberConfig.getString("storage.mongodb.url")
  val databaseName: String = AmberUtils.amberConfig.getString("storage.mongodb.database")
  val client: MongoClient = MongoClients.create(url)
  val database: MongoDatabase = client.getDatabase(databaseName)
  val timeToLive: Int = AmberUtils.amberConfig.getInt("storage.mongodb.stat.ttl-in-minutes")
  val statCollectionName: String =
    AmberUtils.amberConfig.getString("storage.mongodb.stat.stat-collection-name")
  val statPersistFrequency =
    AmberUtils.amberConfig.getInt(
      "storage.mongodb.stat.workflow-stat-persist-update-frequency-seconds"
    )
  val collection: MongoCollection[Document] = database.getCollection(statCollectionName)

  // if the collection doesn't exist, then create it and add an index on the timestamp column
  if (collection.countDocuments() == 0) {
    collection.createIndex(
      Indexes.ascending("created_at"),
      new IndexOptions().expireAfter(timeToLive, TimeUnit.MINUTES)
    )
  }

  TexeraWebApplication
    .scheduleRecurringCallThroughActorSystem(
      2.seconds,
      statPersistFrequency.seconds
    ) {
      persistStats()
    }

  def persistStats(): Unit = {
    if (AmberUtils.amberConfig.getString("storage.mode").equalsIgnoreCase("mongodb")) {
      uncommittedStats.foreach(execution => insertOrUpdate(execution._1, execution._2))
    }
    uncommittedStats.clear()
  }

  def executionStatsAsDocs(
      executionID: Int,
      operatorStatistics: mutable.Map[String, OperatorStatistics]
  ): Document = {
    val doc = new Document()
    val opsDocs = new Document()
    operatorStatistics.foreach(op =>
      opsDocs.append(
        op._1,
        new Document("operatorState", op._2.operatorState)
          .append("aggregatedInputRowCount", op._2.aggregatedInputRowCount.toString)
          .append("aggregatedOutputRowCount", op._2.aggregatedOutputRowCount.toString)
      )
    )
    doc
      .append("execution_ID", executionID)
      .append("created_at", new Date())
      .append("operatorStatistics", opsDocs)
    doc
  }

  def insertOrUpdate(eId: Int, operator_id: String, opStats: OperatorStatistics): Unit = {
    val workflowStats = uncommittedStats.get(eId)
    workflowStats match {
      // execution exists
      case Some(operators) =>
        operators.put(operator_id, opStats)
        uncommittedStats.put(eId, operators)
      // execution not yet in map
      case None =>
        uncommittedStats.put(eId, mutable.Map(operator_id -> opStats))
    }
  }

  def insertOrUpdate(
      eid: Int,
      operatorStatistics: mutable.Map[String, OperatorStatistics]
  ): Unit = {
    if (executionExists(eid)) {
      val condition: BasicDBObject = new BasicDBObject
      condition.put(
        "execution_ID",
        new BasicDBObject("$eq", eid)
      )
      collection.replaceOne(condition, executionStatsAsDocs(eid, operatorStatistics))
    } else {
      collection.insertOne(executionStatsAsDocs(eid, operatorStatistics))
    }
  }

  def executionExists(eId: Int): Boolean = {
    val op: Document = new Document("execution_ID", eId)
    collection.find(op).first() != null
  }

//    def getOperatorStat(eId: Int): Map[String, OperatorStatistics] = {
//      val condition: BasicDBObject = new BasicDBObject
//      condition.put(
//        "execution_ID",
//        new BasicDBObject("$eq", eId)
//      )
//      val opStatsDoc: Document = collection
//        .find(condition)
//        .first()
//
//      //TODO asinstance of Map(opID -> value)
//      OperatorStatistics(
//        opStatsDoc.get("state").asInstanceOf[String],
//        opStatsDoc.get("inputCount").asInstanceOf[Int].toLong,
//        opStatsDoc.get("outputCount").asInstanceOf[Int].toLong
//      )
//    }
}
