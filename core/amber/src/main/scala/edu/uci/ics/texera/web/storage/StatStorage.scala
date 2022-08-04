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
import edu.uci.ics.texera.Utils

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
  final val objectMapper = Utils.objectMapper

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
      uncommittedStats.foreach(execution => insertOrUpdateWorkflowOpStatsInDB(execution._1, execution._2))
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

  def saveWorkflowOpStats(eId: Int, operator_id: String, opStats: OperatorStatistics): Unit = {
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

  def insertOrUpdateWorkflowOpStatsInDB(
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
    val condition: Document = new Document("execution_ID", eId)
    collection.find(condition).first() != null
  }

  def getWorkflowOpsStats(eId: Int): Map[String, OperatorStatistics] = {
    val condition: Document = new Document("execution_ID", eId)
    val opStatsDoc: Document = collection
      .find(condition)
      .first()

    val opStatsMap: mutable.Map[String, OperatorStatistics] = mutable.Map()
    val opStatsDocs: Document = opStatsDoc.get("operatorStatistics").asInstanceOf[Document]
    opStatsDocs
      .keySet()
      .forEach((opID: String) => {
        val opStats: Document = opStatsDocs.get(opID).asInstanceOf[Document]
        opStatsMap.put(
          opID,
          objectMapper.readValue(opStats.toJson(), classOf[OperatorStatistics])
        )
      })
    opStatsMap.toMap
  }
}
