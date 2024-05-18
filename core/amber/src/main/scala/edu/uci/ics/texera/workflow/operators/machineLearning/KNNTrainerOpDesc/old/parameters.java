package edu.uci.ics.texera.workflow.operators.machineLearning.KNNTrainerOpDesc.old;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public enum parameters {

    n_neighbors("n_neighbors","int"),
    p("p","int"),

    ;

    private final String name;
    private final String type;

    parameters(String name,String type) {
        this.name = name;
        this.type  = type;
    }
    @JsonIgnore
    public String getType() {
        return this.type;
    }
    @JsonValue
    public String getName() {
        return this.name;
    }



}
