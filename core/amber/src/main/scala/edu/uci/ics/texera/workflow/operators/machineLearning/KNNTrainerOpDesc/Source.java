package edu.uci.ics.texera.workflow.operators.machineLearning.KNNTrainerOpDesc;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Source {

    workflow("Workflow"),
    panel("Panel"),


    ;

    private final String name;

    Source(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }



}
