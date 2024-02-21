package edu.uci.ics.texera.workflow.operators.machineLearning.SVCtrainer;

import com.fasterxml.jackson.annotation.JsonValue;

public enum KernalFunction {
    rbf("rbf"),
    linear ("linear"),
    poly ("poly"),
    sigmod ("sigmod");

    private final String name;
    KernalFunction(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }
}
