package config

import org.yaml.snakeyaml.Yaml

import java.util.{Map => JMap}
import scala.jdk.CollectionConverters._

object WorkflowComputingUnitManagingServiceConf {

  private val conf: Map[String, Any] = {
    val yaml = new Yaml()
    val inputStream =
      getClass.getClassLoader.getResourceAsStream("computing-unit-manager-config.yaml")
    val javaConf = yaml.load(inputStream).asInstanceOf[JMap[String, Any]].asScala.toMap

    val kubernetesMap = javaConf("kubernetes").asInstanceOf[JMap[String, Any]].asScala.toMap
    javaConf.updated("kubernetes", kubernetesMap)
  }

  val computeUnitPoolNamespace: String = conf("kubernetes")
    .asInstanceOf[Map[String, Any]]("compute-unit-pool-namespace")
    .asInstanceOf[String]
  val computeUnitImageName: String = conf("kubernetes")
    .asInstanceOf[Map[String, Any]]("imageName")
    .asInstanceOf[String]
}
