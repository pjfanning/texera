package edu.uci.ics.texera.workflow.operators.machineLearning.Score_Loop;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Scorer_LoopFunction {
    Accuracy("Accuracy"),
    Precision_score ("Precision score");

    private final String name;
    Scorer_LoopFunction(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }
}
