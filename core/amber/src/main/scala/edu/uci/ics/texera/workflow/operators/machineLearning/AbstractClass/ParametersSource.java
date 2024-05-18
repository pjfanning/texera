package edu.uci.ics.texera.workflow.operators.machineLearning.AbstractClass;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ParametersSource {
    workflow("Workflow"),
    panel("Panel"),

    ;

    private final String name;

    ParametersSource(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }
}
