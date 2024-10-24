package edu.uci.ics.texera

import org.yaml.snakeyaml.Yaml

import java.io.File
import java.nio.file.Path
import java.util.{Map => JMap}
import scala.jdk.CollectionConverters._

object DaoConfig {
  private val conf: Map[String, Any] = {
    val yaml = new Yaml()
    val inputStream = getClass.getClassLoader.getResourceAsStream("dao-config.yaml")
    val javaConf = yaml.load(inputStream).asInstanceOf[JMap[String, Any]].asScala.toMap

    // convert the jdbc section
    val jdbcMap = javaConf("jdbc").asInstanceOf[JMap[String, Any]].asScala.toMap
    javaConf.updated("jdbc", jdbcMap)
  }

  val jdbcUrl: String = conf("jdbc").asInstanceOf[Map[String, String]]("url")
  val jdbcUsername: String = conf("jdbc").asInstanceOf[Map[String, String]]("username")
  val jdbcPassword: String = conf("jdbc").asInstanceOf[Map[String, String]]("password")
}
