package edu.uci.ics.texera.workflow.operators.machineLearning.ScorerLoop;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Scorer_LoopFunction {
    Accuracy("Accuracy"),
    Precision_score ("Precision Score"),
    Confusion_matrix("Confusion Matrix"),
    Recall_score("Recall Score"),
    F1_score("F1 Score"),;

    private final String name;
    Scorer_LoopFunction(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }
}
