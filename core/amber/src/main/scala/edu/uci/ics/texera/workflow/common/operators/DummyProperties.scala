package edu.uci.ics.texera.workflow.common.operators

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import com.fasterxml.jackson.annotation.JsonProperty
trait DummyProperties {
  @JsonProperty(value = "Extra Functionalities", required = false)
  @JsonSchemaTitle("Extra Functionalities")
  @JsonPropertyDescription("Write values of extra functionalities here.")
  @AutofillAttributeName var valuesOfFunctionalities: String = ""
}

