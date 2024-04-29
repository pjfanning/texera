package edu.uci.ics.texera.workflow.operators.drop

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.google.common.base.Preconditions
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalOp.oneToOnePhysicalOp
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecInitInfo
import edu.uci.ics.amber.engine.architecture.deploysemantics.{PhysicalOp, SchemaPropagationFunc}
import edu.uci.ics.amber.engine.common.virtualidentity.{ExecutionIdentity, WorkflowIdentity}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata._
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeNameList
import edu.uci.ics.texera.workflow.common.operators.map.MapOpDesc
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, Schema}

class DropOpDesc extends MapOpDesc {

  @JsonProperty(required = true, defaultValue = "true")
  @JsonSchemaTitle("Drop The Selected Attributes")
  @JsonPropertyDescription("Choose to drop the selected attributes otherwise keep them and drop the unselected ones.")
  var isDrop: Boolean = true

  @JsonProperty(value = "Selected Attributes", required = true)
  @JsonSchemaTitle("Selected Attributes")
  @JsonPropertyDescription("Select the attributes to be dropped or kept.")
  @AutofillAttributeNameList
  var selectedFeatures: List[String] = _



  override def getPhysicalOp(
      workflowId: WorkflowIdentity,
      executionId: ExecutionIdentity
  ): PhysicalOp = {



    oneToOnePhysicalOp(
      workflowId,
      executionId,
      operatorIdentifier,
      OpExecInitInfo((_, _) => new DropOpExec(selectedFeatures,isDrop))
    )
      .withInputPorts(operatorInfo.inputPorts)
      .withOutputPorts(operatorInfo.outputPorts)
      .withPropagateSchema(SchemaPropagationFunc(inputSchemas => {
        val inputSchema = inputSchemas(operatorInfo.inputPorts.head.id)
        var selectedAttributes = selectedFeatures
        if (isDrop){
        val allAttributes =  inputSchema.getAttributeNames
        selectedAttributes = allAttributes.diff(selectedFeatures)
        }

        val outputSchema = Schema
          .builder()
          .add(
            selectedAttributes
              .map(attribute =>
                new Attribute(
                  attribute,
                  inputSchema.getAttribute(attribute).getType
                )
              )
          )
          .build()
        Map(operatorInfo.outputPorts.head.id -> outputSchema)
      }))







  }




  override def operatorInfo: OperatorInfo = {
    OperatorInfo(
      "Drop",
      "Drop the selected column",
      OperatorGroupConstants.CLEANING_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )
  }

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Preconditions.checkArgument(schemas.length == 1)
    Preconditions.checkArgument(selectedFeatures.nonEmpty)
    var selectedAttributes = selectedFeatures
    val inputSchema = schemas(0)
    if (isDrop) {
      val allAttributes = inputSchema.getAttributeNames
      selectedAttributes = allAttributes.diff(selectedFeatures)
    }



    Schema
      .builder()
      .add(selectedAttributes.map { attribute =>
        val originalType = schemas.head.getAttribute(attribute).getType
        new Attribute(attribute, originalType)
      })
      .build()
  }

}
