package edu.uci.ics.texera.workflow.operators.sklearnAdvance.SVMTrainerOpDesc;

import edu.uci.ics.texera.workflow.operators.sklearnAdvance.AbstractClass.EnumClass;

public enum SVCParameters implements EnumClass {
    C("C", "float"),
    kernel("kernel", "str"),
    gamma("gamma", "float"),
    degree("degree", "int"),
    coef0("coef0", "float"),
    tol("tol", "float"),
    probability("probability", "'True'==")
    ;

    private final String name;
    private final String type;

    SVCParameters(String name, String type) {
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
