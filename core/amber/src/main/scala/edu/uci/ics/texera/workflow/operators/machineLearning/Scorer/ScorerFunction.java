package edu.uci.ics.texera.workflow.operators.machineLearning.Scorer;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ScorerFunction {
    accuracy("Accuracy"),
    precisionScore ("Precision Score"),
    recallScore("Recall Score"),
    f1Score("F1 Score"),;

    private final String name;
    ScorerFunction(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }
}
