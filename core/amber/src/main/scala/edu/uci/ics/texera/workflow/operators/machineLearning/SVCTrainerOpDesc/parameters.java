package edu.uci.ics.texera.workflow.operators.machineLearning.SVCTrainerOpDesc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public enum parameters {

    C("C", "float"),
    kernel("kernel", "str"),
    gamma("gamma", "float"),
    degree("degree", "int"),
    coef0("coef0", "float"),
    tol("tol", "float"),
    probability("probability", "'True'=="),


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
