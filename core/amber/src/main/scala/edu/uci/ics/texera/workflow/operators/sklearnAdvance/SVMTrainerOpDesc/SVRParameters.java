package edu.uci.ics.texera.workflow.operators.sklearnAdvance.SVMTrainerOpDesc;

import edu.uci.ics.texera.workflow.operators.sklearnAdvance.AbstractClass.EnumClass;

public enum SVRParameters implements EnumClass {
    C("C", "float"),
    kernel("kernel", "str"),
    gamma("gamma", "float"),
    degree("degree", "int"),
    coef0("coef0", "float"),
    tol("tol", "float"),
    epsilon("epsilon", "float"),
    shrinking("shrinking", "'True'=="),
    cache_size("cache_size", "int"),
    max_iter("max_iter", "int"),
    verbose("verbose", "'True'==")
    ;

    private final String name;
    private final String type;

    SVRParameters(String name, String type) {
        this.name = name;
        this.type  = type;
    }

    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }
}