package edu.uci.ics.texera.workflow.operators.visualization.boxPlot;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BoxPlotQuartileFunction {
    LINEAR("linear"),
    INCLUSIVE("inclusive"),
    EXCLUSIVE("exclusive");
    private final String quartileType;

    BoxPlotQuartileFunction(String quartileType) {
        this.quartileType = quartileType;
    }

    @JsonValue
    public String getQuartileType() {
        return this.quartileType;
    }
}