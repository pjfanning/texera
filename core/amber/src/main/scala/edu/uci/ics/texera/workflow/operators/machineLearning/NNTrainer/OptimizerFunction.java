package edu.uci.ics.texera.workflow.operators.machineLearning.NNTrainer;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OptimizerFunction {
    Adam("Adam"),
    SGD("SGD"),
  ;

    private final String name;
    OptimizerFunction(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }
}

