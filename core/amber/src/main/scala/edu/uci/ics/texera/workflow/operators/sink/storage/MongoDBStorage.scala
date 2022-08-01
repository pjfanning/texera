package edu.uci.ics.texera.workflow.operators.sink.storage

import com.mongodb.client.model.Sorts
import com.mongodb.client.{MongoClient, MongoClients, MongoCollection, MongoCursor, MongoDatabase}
import edu.uci.ics.amber.engine.common.AmberUtils
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.TupleUtils.document2Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.Schema
import org.bson.Document

import scala.collection.mutable
import collection.JavaConverters._

class MongoDBStorage(id: String, schema: Schema) extends SinkStorageReader {

  schema.getAttributeNames.stream.forEach(name =>
    assert(!name.matches(".*[\\$\\.].*"), s"illegal attribute name '$name' for mongo DB")
  )

  val url: String = AmberUtils.amberConfig.getString("storage.mongodb.url")
  val databaseName: String = AmberUtils.amberConfig.getString("storage.mongodb.database")
  val client: MongoClient = MongoClients.create(url)
  val database: MongoDatabase = client.getDatabase(databaseName)
  val commitBatchSize: Int = AmberUtils.amberConfig.getInt("storage.mongodb.commit-batch-size")

  val catalogCollectionName: String =
    AmberUtils.amberConfig.getString("storage.mongodb.cleanup.catalog-collection-name")
  val catalogCollection: MongoCollection[Document] = database.getCollection(catalogCollectionName)

  /**
    * this function adds every collection name of Mongo to a catalog so that we periodically clean it up
    * @param collectionName name of the new collection to be added in the catalog
    */
  def createNewCollection(collectionName: String): Unit = {

    val newCollectionEntry: Document = new Document()
    newCollectionEntry.append("collectionName", collectionName)
    newCollectionEntry.append("createdAt", System.currentTimeMillis())
    catalogCollection.insertOne(newCollectionEntry)

  }

  class MongoDBSinkStorageWriter(bufferSize: Int) extends SinkStorageWriter {
    var client: MongoClient = _
    var uncommittedInsertions: mutable.HashSet[Tuple] = _
    var collection: MongoCollection[Document] = _

    override def open(): Unit = {
      uncommittedInsertions = new mutable.HashSet[Tuple]()
      client = MongoClients.create(url)
      val database: MongoDatabase = client.getDatabase(databaseName)
      collection = database.getCollection(id)
    }

    override def close(): Unit = {
      if (uncommittedInsertions.nonEmpty) {
        collection.insertMany(uncommittedInsertions.map(_.asDocument()).toList.asJava)
        uncommittedInsertions.clear()
      }
      createNewCollection(
        id
      ) // TODO we need to handle cases when the collection is created
      //  but not closed gracefully, then it will be forever in disk
      client.close()
    }

    override def putOne(tuple: Tuple): Unit = {
      uncommittedInsertions.add(tuple)
      if (uncommittedInsertions.size == bufferSize) {
        collection.insertMany(uncommittedInsertions.map(_.asDocument()).toList.asJava)
        uncommittedInsertions.clear()
      }
    }

    override def removeOne(tuple: Tuple): Unit = {
      if (uncommittedInsertions.contains(tuple)) {
        uncommittedInsertions.remove(tuple)
      } else {
        collection.findOneAndDelete(tuple.asDocument())
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

  override def getStorageWriter(): SinkStorageWriter =
    new MongoDBSinkStorageWriter(commitBatchSize)

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

  override def getAllAfter(offset: Int): Iterable[Tuple] = {
    val collection = database.getCollection(id)
    val cursor = collection.find().sort(Sorts.ascending("_id")).skip(offset).cursor()
    mkTupleIterable(cursor)
  }

  override def getSchema: Schema = schema
}
