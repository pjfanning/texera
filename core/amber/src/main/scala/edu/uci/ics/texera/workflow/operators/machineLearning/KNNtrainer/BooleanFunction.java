package edu.uci.ics.texera.workflow.operators.machineLearning.KNNtrainer;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BooleanFunction {

    No("No"),

    Yes("Yes");

    private final String name;

    BooleanFunction(String name) {
        this.name = name;
    }

    // use the name string instead of enum string in JSON
    @JsonValue
    public String getName() {
        return this.name;
    }

}
