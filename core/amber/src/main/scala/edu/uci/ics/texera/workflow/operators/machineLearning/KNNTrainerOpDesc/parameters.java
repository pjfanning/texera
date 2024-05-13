package edu.uci.ics.texera.workflow.operators.machineLearning.KNNTrainerOpDesc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public enum parameters {

    K("k"),

    ;

    private final String name;

    parameters(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }



}
