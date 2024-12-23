package config

import com.typesafe.config.{Config, ConfigFactory}

object WorkflowComputingUnitManagingServiceConf {

  // Load the configuration
  private val conf: Config = ConfigFactory.load()

  // Access the Kubernetes settings with environment variable fallback
  val computeUnitPoolNamespace: String = conf.getString("kubernetes.compute-unit-pool-namespace")
  val computeUnitImageName: String = conf.getString("kubernetes.image-name")
}