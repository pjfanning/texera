package edu.uci.ics.texera.workflow.operators.machineLearning.AbstractClass;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaBool;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeNameOnPort1;
import edu.uci.ics.texera.workflow.common.metadata.annotations.HideAnnotation;

public class HyperParameters<T>{

    @JsonProperty(required = true)
    @JsonSchemaTitle("Parameter")
    @JsonPropertyDescription("Choose the name of the parameter")
    public T parameter;

    @JsonProperty(value = "parametersSource", required = true)
    @JsonSchemaTitle("Source")
    @JsonPropertyDescription("Choose source of the parameter")
    public ParametersSource parametersSource;

    @JsonSchemaInject(strings = {
            @JsonSchemaString(path = HideAnnotation.hideTarget, value = "parametersSource"),
            @JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
            @JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "Panel")
    },
            bools = @JsonSchemaBool(path = HideAnnotation.hideOnNull, value = true)
    )
    @JsonProperty(value = "attribute")
    @AutofillAttributeNameOnPort1
    public String attribute;


    @JsonSchemaInject(strings = {
            @JsonSchemaString(path = HideAnnotation.hideTarget, value = "parametersSource"),
            @JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
            @JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "Workflow")
    },
            bools = @JsonSchemaBool(path = HideAnnotation.hideOnNull, value = true))
    @JsonProperty(value = "value")
    public String value;

    public HyperParameters(T parameter, String attribute, ParametersSource parametersSource, String value) {
        this.parameter = parameter;
        this.attribute = attribute;
        this.parametersSource = parametersSource;
        this.value = value;
    }}







