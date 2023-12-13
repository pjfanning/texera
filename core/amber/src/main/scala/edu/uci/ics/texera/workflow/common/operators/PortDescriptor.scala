package edu.uci.ics.texera.workflow.common.operators

import com.fasterxml.jackson.annotation.JsonProperty
import edu.uci.ics.texera.workflow.common.IncrementalOutputMode
import edu.uci.ics.texera.workflow.common.workflow.{PartitionInfo, UnknownPartition}
import edu.uci.ics.texera.workflow.operators.sink.storage.SinkStorageReader
import net.minidev.json.annotate.JsonIgnore


case class OutputDescriptor(
                             // use SET_SNAPSHOT as the default output mode
                             // this will be set internally by the workflow compiler
                             outputMode:IncrementalOutputMode,
                             // whether this sink corresponds to a visualization result, default is no
                             chartType: Option[String]
                           )
case class OutputPortInfo(
                           outputDescriptor: OutputDescriptor = OutputDescriptor(IncrementalOutputMode.SET_SNAPSHOT, None),
                           var storage:Option[SinkStorageReader] = None)

case class PortDescription(
    portID: String,
    displayName: String,
    allowMultiInputs: Boolean,
    isDynamicPort: Boolean,
    partitionRequirement: PartitionInfo,
    dependencies: List[Int]
)

trait PortDescriptor {
  @JsonProperty(required = false)
  var inputPorts: List[PortDescription] = List.empty

  @JsonProperty(required = false)
  var outputPorts: List[PortDescription] = List(PortDescription("","",allowMultiInputs = false, isDynamicPort = false, UnknownPartition(),List.empty))

  @JsonIgnore
  var outputPortsInfo: List[OutputPortInfo] = {
    outputPorts.map(_ => OutputPortInfo())
  }
}
