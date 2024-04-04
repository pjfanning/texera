package edu.uci.ics.texera.workflow.operators.machineLearning.CategoryToNumber

import com.fasterxml.jackson.annotation.JsonProperty
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName

class AttributeSectionList {
  @JsonProperty(required = true)
  @JsonSchemaTitle("Attribute Name")
  @AutofillAttributeName
  var attributeName: String = ""
}
