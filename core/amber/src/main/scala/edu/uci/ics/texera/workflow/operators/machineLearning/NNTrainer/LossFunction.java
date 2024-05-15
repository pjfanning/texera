package edu.uci.ics.texera.workflow.operators.machineLearning.NNTrainer;

import com.fasterxml.jackson.annotation.JsonValue;

public enum LossFunction {

    CrossEntropyLoss("CrossEntropyLoss"),
    MSELoss("MSELoss"),

    L1Loss("L1Loss")
    ;

    private final String name;
    LossFunction(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }


}
