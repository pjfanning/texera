package edu.uci.ics.texera.dao

import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.{Configuration, Jdbc}
import com.typesafe.config.{Config, ConfigFactory}
import java.nio.file.{Files, Path}

object JooqCodeGenerator {
  @throws[Exception]
  def main(args: Array[String]): Unit = {
    // Load jOOQ configuration XML
    val jooqXmlPath: Path =
      Path.of("dao").resolve("src").resolve("main").resolve("resources").resolve("jooq-conf.xml")
    val jooqConfig: Configuration = GenerationTool.load(Files.newInputStream(jooqXmlPath))

    // Load .conf configuration
    val confPath: Path = Path
      .of("workflow-core")
      .resolve("src")
      .resolve("main")
      .resolve("resources")
      .resolve("storage.conf")
    val conf: Config = ConfigFactory.parseFile(confPath.toFile)

    // Resolve placeholders in the configuration file
    val resolvedConf: Config = conf.resolve()

    // Get JDBC configuration from .conf file
    val jdbcUsername = resolvedConf.getString("storage.jdbc.username")
    val jdbcPassword = resolvedConf.getString("storage.jdbc.password")
    val jdbcUrl = resolvedConf.getString("storage.jdbc.url")

    // Set JDBC configuration for jOOQ
    val jooqJdbcConfig = new Jdbc
    jooqJdbcConfig.setDriver("com.mysql.cj.jdbc.Driver")
    jooqJdbcConfig.setUrl(jdbcUrl)
    jooqJdbcConfig.setUsername(jdbcUsername)
    jooqJdbcConfig.setPassword(jdbcPassword)

    jooqConfig.setJdbc(jooqJdbcConfig)

    // Generate the code
    GenerationTool.generate(jooqConfig)
  }
}
