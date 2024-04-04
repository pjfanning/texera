package edu.uci.ics.texera.workflow.operators.machineLearning.RegressionScorer;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RegressionScorerFunction {

    MSE("MSE"),
    RMSE("RMSE"),
    MAE("MAE"),
    R2("R2"),;

    private final String name;
    RegressionScorerFunction(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }

}
