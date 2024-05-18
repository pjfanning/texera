package edu.uci.ics.texera.workflow.operators.machineLearning.KNNTrainerOpDesc;

import edu.uci.ics.texera.workflow.operators.machineLearning.AbstractClass.AbstractEnumClass;

public enum KNNParameters implements AbstractEnumClass {
    n_neighbors("n_neighbors", "int"),
    p("p", "int");

    private final String name;
    private final String type;

    KNNParameters(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    };
}
