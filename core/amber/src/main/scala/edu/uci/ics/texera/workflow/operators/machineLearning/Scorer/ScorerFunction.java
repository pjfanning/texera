package edu.uci.ics.texera.workflow.operators.machineLearning.Scorer;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ScorerFunction {
    Accuracy("Accuracy"),
    Precision_score ("Precision Score"),
    Recall_score("Recall Score"),
    F1_score("F1 Score"),;

    private final String name;
    ScorerFunction(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }
}
