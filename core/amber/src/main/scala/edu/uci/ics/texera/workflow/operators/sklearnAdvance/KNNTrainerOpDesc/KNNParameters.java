
package edu.uci.ics.texera.workflow.operators.sklearnAdvance.KNNTrainerOpDesc;

import edu.uci.ics.texera.workflow.operators.sklearnAdvance.AbstractClass.EnumClass;

public enum KNNParameters implements EnumClass {
    n_neighbors("n_neighbors", "int"),
    p("p", "int"),
    weights("weights", "str"),
    algorithm("algorithm", "str"),
    leaf_size("leaf_size", "int"),
    metric("metric", "int"),
    metric_params("metric_params", "str"),
    n_jobs("n_jobs", "int"),;

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
