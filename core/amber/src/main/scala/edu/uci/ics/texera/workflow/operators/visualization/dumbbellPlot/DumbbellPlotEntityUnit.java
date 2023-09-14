package edu.uci.ics.texera.workflow.operators.visualization.dumbbellPlot;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeNameLambda;

import java.util.Objects;

public class DumbbellPlotEntityUnit {
  @JsonProperty(required = true)
  @JsonSchemaTitle("Entity Name")
  @AutofillAttributeNameLambda
  public String entityName;

  public DumbbellPlotEntityUnit(String entityName) {
    this.entityName = entityName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DumbbellPlotEntityUnit)) return false;
    DumbbellPlotEntityUnit that = (DumbbellPlotEntityUnit) o;
    return Objects.equals(entityName, that.entityName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(entityName);
  }
}
