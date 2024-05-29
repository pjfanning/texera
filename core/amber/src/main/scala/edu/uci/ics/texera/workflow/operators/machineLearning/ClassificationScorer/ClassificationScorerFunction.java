package edu.uci.ics.texera.workflow.operators.machineLearning.ClassificationScorer;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ClassificationScorerFunction {
    accuracy("Accuracy"),
    precisionScore ("Precision Score"),
    recallScore("Recall Score"),
    f1Score("F1 Score"),;

    private final String name;
    ClassificationScorerFunction(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }
}