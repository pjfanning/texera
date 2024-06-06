package edu.uci.ics.texera.workflow.common.storage

import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.common.AmberConfig
import edu.uci.ics.amber.engine.common.storage.{BufferedItemWriter, VirtualDocument}
import edu.uci.ics.amber.engine.common.storage.mongodb.{MemoryDocument, MongoDBBufferedItemWriter, MongoDocument}
import edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.Schema

import java.util.concurrent.ConcurrentHashMap

object OpResultStorage {
  val defaultStorageMode: String = AmberConfig.sinkStorageMode.toLowerCase
  val MEMORY = "memory"
  val MONGODB = "mongodb"

  def getWriter(executionId: String = "",
                key: OperatorIdentity,
                mode: String): BufferedItemWriter[Tuple] = {
    if (mode == "memory") {
      new MemoryDocument[Tuple]
    } else {
      new MongoDBBufferedItemWriter[Tuple](1024, executionId + key, Tuple.toDocument)
    }
  }
}

/**
  * Public class of operator result storage.
  * One execution links one instance of OpResultStorage, both have the same lifecycle.
  */
class OpResultStorage extends Serializable with LazyLogging {

  val cache: ConcurrentHashMap[OperatorIdentity, (VirtualDocument[Tuple], Schema)] =
    new ConcurrentHashMap[OperatorIdentity, (VirtualDocument[Tuple], Schema)]()

  /**
    * Retrieve the result of an operator from OpResultStorage
    * @param key The key used for storage and retrieval.
    *            Currently it is the uuid inside the cache source or cache sink operator.
    * @return The storage of this operator.
    */
  def get(key: OperatorIdentity): (VirtualDocument[Tuple], Schema) = {
    cache.get(key)
  }

  def create(
      executionId: String = "",
      key: OperatorIdentity,
      mode: String,
      schema: Schema
  ): VirtualDocument[Tuple] = {
    val storage: VirtualDocument[Tuple] =
      if (mode == "memory") {
        new MemoryDocument[Tuple]
      } else {
        try {
          new MongoDocument[Tuple](executionId + key, Tuple.toDocument, Tuple.fromDocument(schema))
        } catch {
          case t: Throwable =>
            logger.warn("Failed to create mongo storage", t)
            logger.info(s"Fall back to memory storage for $key")
            // fall back to memory
            new MemoryDocument[Tuple]
        }
      }
    cache.put(key, (storage, schema))
    storage
  }

  def contains(key: OperatorIdentity): Boolean = {
    cache.containsKey(key)
  }

  /**
    * Manually remove an entry from the cache.
    * @param key The key used for storage and retrieval.
    *            Currently it is the uuid inside the cache source or cache sink operator.
    */
  def remove(key: OperatorIdentity): Unit = {
    if (cache.contains(key)) {
      cache.get(key)._1.remove()
    }
    cache.remove(key)
  }

  /**
    * Close this storage. Used for workflow cleanup.
    */
  def close(): Unit = {
    cache.forEach((_, document) => document._1.remove())
    cache.clear()
  }

}
