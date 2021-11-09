package edu.uci.ics.texera.workflow.operators.sink.storage

import java.util

import com.mongodb.client.model.Sorts
import com.mongodb.client.{MongoClient, MongoClients, MongoCollection, MongoCursor, MongoDatabase}
import edu.uci.ics.amber.engine.common.AmberUtils
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.TupleUtils.document2Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.Schema
import org.bson.Document

class MongoDBStorage(id: String, schema: Schema) extends SinkStorage {

  assert(
    schema.getAttributeNames.stream.noneMatch((name: String) => name.matches(".*[\\$\\.].*")),
    "illegal attribute name for mongo DB"
  )

  val url: String = AmberUtils.amberConfig.getString("cache.mongodb.url")
  val databaseName: String = AmberUtils.amberConfig.getString("cache.mongodb.database")
  val client: MongoClient = MongoClients.create(url)
  val database: MongoDatabase = client.getDatabase(databaseName)
  database.getCollection(id).drop()

  class MongoDBShardedStorage(bufferSize: Int) extends ShardedStorage {
    var client: MongoClient = _
    var buffer: util.ArrayList[Document] = _
    var collection: MongoCollection[Document] = _

    override def open(): Unit = {
      buffer = new util.ArrayList[Document]()
      client = MongoClients.create(url)
      val database: MongoDatabase = client.getDatabase(databaseName)
      collection = database.getCollection(id)
    }

    override def close(): Unit = {
      if (!buffer.isEmpty) {
        collection.insertMany(buffer)
        buffer.clear()
      }
      client.close()
    }

    override def putOne(tuple: Tuple): Unit = {
      buffer.add(tuple.asDocument())
      if (buffer.size == bufferSize) {
        collection.insertMany(buffer)
        buffer.clear()
      }
    }
  }

  private[this] def mkTupleIterable(cursor: MongoCursor[Document]): Iterable[Tuple] = {
    new Iterator[Tuple] {
      override def hasNext: Boolean = cursor.hasNext
      override def next(): Tuple = document2Tuple(cursor.next(), schema)
    }.toIterable
  }

  override def getAll: Iterable[Tuple] = {
    val collection = database.getCollection(id)
    val cursor = collection.find().sort(Sorts.ascending("_id")).cursor()
    mkTupleIterable(cursor)
  }

  override def getShardedStorage(idx: Int): ShardedStorage = new MongoDBShardedStorage(1000)

  override def clear(): Unit = {
    database.getCollection(id).drop()
  }

  override def getRange(from: Int, to: Int): Iterable[Tuple] = {
    val collection = database.getCollection(id)
    val cursor = collection.find().sort(Sorts.ascending("_id")).limit(to - from).skip(from).cursor()
    mkTupleIterable(cursor)
  }

  override def getCount: Long = {
    database.getCollection(id).countDocuments()
  }
}
