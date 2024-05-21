package edu.uci.ics.texera.workflow.operators.machineLearning.SVCTrainer;

import edu.uci.ics.texera.workflow.operators.machineLearning.AbstractClass.AbstractEnumClass;

public enum SVCParameters implements AbstractEnumClass {
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

    SVCParameters(String name,String type) {
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
