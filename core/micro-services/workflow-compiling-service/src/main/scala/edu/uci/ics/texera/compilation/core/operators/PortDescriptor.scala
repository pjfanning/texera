package edu.uci.ics.texera.compilation.core.operators

import com.fasterxml.jackson.annotation.JsonProperty
import edu.uci.ics.amber.core.workflow.PartitionInfo

case class PortDescription(
    portID: String,
    displayName: String,
    allowMultiInputs: Boolean,
    isDynamicPort: Boolean,
    partitionRequirement: PartitionInfo,
    dependencies: List[Int] = List.empty
)

trait PortDescriptor {
  @JsonProperty(required = false)
  var inputPorts: List[PortDescription] = null

  @JsonProperty(required = false)
  var outputPorts: List[PortDescription] = null
}
