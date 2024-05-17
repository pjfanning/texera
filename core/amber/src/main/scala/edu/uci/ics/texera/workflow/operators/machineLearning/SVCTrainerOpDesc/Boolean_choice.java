package edu.uci.ics.texera.workflow.operators.machineLearning.SVCTrainerOpDesc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Boolean_choice {
    True("True"),
    False("False"),


    ;

    private final String name;

    Boolean_choice(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }



}
