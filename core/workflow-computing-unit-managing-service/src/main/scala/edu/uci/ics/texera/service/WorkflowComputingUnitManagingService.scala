package edu.uci.ics.texera.service

import config.WorkflowComputingUnitManagingServiceConf
import edu.uci.ics.texera.service.resource.WorkflowComputingUnitManagingResource
import io.dropwizard.core.setup.{Bootstrap, Environment}
import io.dropwizard.core.{Application, Configuration}

class WorkflowComputingUnitManagingService extends Application[Configuration] {

  override def initialize(bootstrap: Bootstrap[Configuration]): Unit = {}

  override def run(configuration: Configuration, environment: Environment): Unit = {
    val appConfig = WorkflowComputingUnitManagingServiceConf.computeUnitPoolNamespace

    // Register http resources
    environment.jersey().register(new WorkflowComputingUnitManagingResource)
  }
}

object WorkflowComputingUnitManagingService {
  def main(args: Array[String]): Unit = {
    new WorkflowComputingUnitManagingService().run(args: _*)
  }
}
