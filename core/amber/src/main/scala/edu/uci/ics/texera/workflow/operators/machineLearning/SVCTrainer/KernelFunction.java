package edu.uci.ics.texera.workflow.operators.machineLearning.SVCTrainer;

import com.fasterxml.jackson.annotation.JsonValue;

public enum KernelFunction {
    rbf("rbf"),
    linear ("linear"),
    poly ("poly"),
    sigmoid ("sigmoid");

    private final String name;
    KernelFunction(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }
}