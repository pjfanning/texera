package edu.uci.ics.texera.workflow.operators.machineLearning.MLPCreator;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ActivationFunction {

    ReLU("ReLU"),
    Tanh("Tanh"),

    Sigmoid("Sigmoid")
    ;

    private final String name;
    ActivationFunction(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }


}
