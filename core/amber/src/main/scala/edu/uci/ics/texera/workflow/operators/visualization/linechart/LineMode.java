package edu.uci.ics.texera.workflow.operators.visualization.lineChart;

import com.fasterxml.jackson.annotation.JsonValue;

public enum LineMode {
    LINE("line"),
    DOTS("dots"),
    LINE_WITH_DOTS("line with dots");
    private final String mode;

    LineMode(String mode) {
        this.mode = mode;
    }

    @JsonValue
    public String getMode() {
        return mode;
    }

    public String getModeInPlotly(){
        // make the mode string compatible with plotly API.
        switch(this){
            case DOTS:
                return "markers";
            case LINE:
                return "lines";
            case LINE_WITH_DOTS:
                return "lines+markers";
            default:
                throw new UnsupportedOperationException("line mode is not supported");
        }
    }
}