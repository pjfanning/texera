package edu.uci.ics.amber.core.storage

import com.typesafe.config.{Config, ConfigFactory}

object StorageConfig {

  // Load configuration
  private val conf: Config = ConfigFactory.parseResources("storage.conf").resolve()

  // General storage settings
  val resultStorageMode: String = conf.getString("storage.result-storage-mode")

  // MongoDB specifics
  val mongodbUrl: String = conf.getString("storage.mongodb.url")
  val mongodbDatabaseName: String = conf.getString("storage.mongodb.database")
  val mongodbBatchSize: Int = conf.getInt("storage.mongodb.commit-batch-size")

  // JDBC specifics
  val jdbcUrl: String = conf.getString("storage.jdbc.url")
  val jdbcUsername: String = conf.getString("storage.jdbc.username")
  val jdbcPassword: String = conf.getString("storage.jdbc.password")
}