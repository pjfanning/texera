package edu.uci.ics.texera.workflow.operators.visualization.scatterplot;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalOp;
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecInitInfo;
import edu.uci.ics.amber.engine.common.IOperatorExecutor;
import edu.uci.ics.amber.engine.common.virtualidentity.ExecutionIdentity;
import edu.uci.ics.amber.engine.common.virtualidentity.WorkflowIdentity;
import edu.uci.ics.texera.workflow.common.metadata.InputPort;
import edu.uci.ics.texera.workflow.common.metadata.OperatorGroupConstants;
import edu.uci.ics.texera.workflow.common.metadata.OperatorInfo;
import edu.uci.ics.texera.workflow.common.metadata.OutputPort;
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName;
import edu.uci.ics.texera.workflow.common.tuple.schema.Attribute;
import edu.uci.ics.texera.workflow.common.tuple.schema.AttributeType;
import edu.uci.ics.texera.workflow.common.tuple.schema.OperatorSchemaInfo;
import edu.uci.ics.texera.workflow.common.tuple.schema.Schema;
import edu.uci.ics.texera.workflow.operators.visualization.VisualizationConstants;
import edu.uci.ics.texera.workflow.operators.visualization.VisualizationOperator;
import scala.Tuple2;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;

import static edu.uci.ics.texera.workflow.common.tuple.schema.AttributeType.DOUBLE;
import static edu.uci.ics.texera.workflow.common.tuple.schema.AttributeType.INTEGER;
import static java.util.Collections.singletonList;
import static scala.collection.JavaConverters.asScalaBuffer;

/**
 * Scatterplot operator to visualize the result as a scatterplot
 * This is the description of the operator
 */

@JsonSchemaInject(json =
        "{" +
                "  \"attributeTypeRules\": {" +
                "    \"xColumn\":{" +
                "      \"enum\": [\"integer\", \"double\"]" +
                "    }," +
                "    \"yColumn\":{" +
                "      \"enum\": [\"integer\", \"double\"]" +
                "    }" +
                "  }" +
                "}")
public class ScatterplotOpDesc extends VisualizationOperator {
    @JsonProperty(required = true)
    @JsonSchemaTitle("X-Column")
    @AutofillAttributeName
    public String xColumn;

    @JsonProperty(required = true)
    @JsonSchemaTitle("Y-Column")
    @AutofillAttributeName
    public String yColumn;

    @JsonProperty(defaultValue = "false")
    @JsonSchemaTitle("Geometric")
    @JsonPropertyDescription("plot on a map")
    public boolean isGeometric;

    @Override
    public String chartType() {
        if (isGeometric) {
            return VisualizationConstants.SPATIAL_SCATTERPLOT;
        }
        return VisualizationConstants.SIMPLE_SCATTERPLOT;
    }

    @Override
    public PhysicalOp getPhysicalOp(WorkflowIdentity workflowId, ExecutionIdentity executionId, OperatorSchemaInfo operatorSchemaInfo) {
        AttributeType xType = operatorSchemaInfo.inputSchemas()[0].getAttribute(xColumn).getType();
        AttributeType yType = operatorSchemaInfo.inputSchemas()[0].getAttribute(yColumn).getType();
        Set<AttributeType> allowedAttributeTypesNumbersOnly = EnumSet.of(DOUBLE, INTEGER); //currently, the frontend has limitation it doesn't accept axes of type long
        if (!allowedAttributeTypesNumbersOnly.contains(xType)) {
            throw new IllegalArgumentException(xColumn + " is not a number \n");
        }
        if (!allowedAttributeTypesNumbersOnly.contains(yType)) {
            throw new IllegalArgumentException(yColumn + " is not a number \n");
        }

        return PhysicalOp.oneToOnePhysicalOp(
                    workflowId,
                    executionId,
                    this.operatorIdentifier(),
                    OpExecInitInfo.apply((Function<Tuple2<Object, PhysicalOp>, IOperatorExecutor> & java.io.Serializable) worker -> new ScatterplotOpExec(this, operatorSchemaInfo))
                )
                .withIsOneToManyOp(true).withParallelizable(!isGeometric);
    }

    @Override
    public OperatorInfo operatorInfo() {
        return new OperatorInfo(
                "Scatterplot",
                "View the result in a scatterplot",
                OperatorGroupConstants.VISUALIZATION_GROUP(),
                asScalaBuffer(singletonList(new InputPort("", false))).toList(),
                asScalaBuffer(singletonList(new OutputPort(""))).toList(),
                false, false, false, false);
    }

    @Override
    public Schema getOutputSchema(Schema[] schemas) {
        Schema inputSchema = schemas[0];
        if (isGeometric)
            return Schema.newBuilder().add(
                    new Attribute("xColumn", inputSchema.getAttribute(xColumn).getType()),
                    new Attribute("yColumn", inputSchema.getAttribute(yColumn).getType())
            ).build();
        else
            return Schema.newBuilder().add(
                    new Attribute(xColumn, inputSchema.getAttribute(xColumn).getType()),
                    new Attribute(yColumn, inputSchema.getAttribute(yColumn).getType())
            ).build();
    }
}
