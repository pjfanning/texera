package edu.uci.ics.texera.workflow.operators.machineLearning.Scorer;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ScorerFunction {
    Accuracy("Accuracy"),
    Precision_score ("Precision Score"),
    Confusion_matrix("Confusion Matrix");


    private final String name;
    ScorerFunction(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }
}
