package edu.uci.ics.texera.workflow.operators.machineLearning.KNNTrainerOpDesc.old;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInt;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeNameOnPort1;
import edu.uci.ics.texera.workflow.common.metadata.annotations.CommonOpDescAnnotation;
import edu.uci.ics.texera.workflow.common.metadata.annotations.HideAnnotation;

public class HyperP {


    @JsonProperty(required = true)
    @JsonSchemaTitle("Parameter")
    @JsonPropertyDescription("Choose the name of the parameter")
    public parameters parameter;


    @JsonProperty(value = "source", required = true)
    @JsonSchemaTitle("Source")
//    @JsonSchemaInject(json = """{"toggleHidden" : ["attribute","value"]}""")
    @JsonPropertyDescription("Choose source of the parameter")
    public Source source;


    @JsonSchemaInject(strings = {
            @JsonSchemaString(path = HideAnnotation.hideTarget, value = "source"),
            @JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
            @JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "Panel")
    })
    @JsonProperty(value = "attribute")
    @AutofillAttributeNameOnPort1
    public String attribute;


    @JsonSchemaInject(strings = {
            @JsonSchemaString(path = HideAnnotation.hideTarget, value = "source"),
            @JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
            @JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "Workflow")
    })
    @JsonProperty(value = "value")
    public String value;

    public HyperP(parameters parameter,String attribute, Source source, String value) {
        this.parameter = parameter;
        this.attribute = attribute;
        this.source = source;
        this.value = value;
    }}





