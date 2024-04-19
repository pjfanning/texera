package edu.uci.ics.texera.workflow.operators.machineLearning.MLPCreator;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ActivateFunction {

    Relu("Relu"),
    Tanh("Tanh"),

    Sigmoid("Sigmoid")
    ;

    private final String name;
    ActivateFunction(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }


}
