package edu.uci.ics.texera.workflow.operators.udf.python.source;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.base.Preconditions;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalOp;
import edu.uci.ics.amber.engine.architecture.deploysemantics.SchemaPropagationFunc;
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecInitInfo;
import edu.uci.ics.amber.engine.common.AmberRuntime;
import edu.uci.ics.amber.engine.common.virtualidentity.ExecutionIdentity;
import edu.uci.ics.amber.engine.common.virtualidentity.WorkflowIdentity;
import edu.uci.ics.amber.engine.common.workflow.InputPort;
import edu.uci.ics.amber.engine.common.workflow.OutputPort;
import edu.uci.ics.amber.engine.common.workflow.PortIdentity;
import edu.uci.ics.texera.workflow.common.metadata.OperatorGroupConstants;
import edu.uci.ics.texera.workflow.common.metadata.OperatorInfo;
import edu.uci.ics.texera.workflow.common.operators.source.SourceOperatorDescriptor;
import edu.uci.ics.texera.workflow.common.tuple.schema.Attribute;
import edu.uci.ics.texera.workflow.common.tuple.schema.Schema;
import edu.uci.ics.texera.workflow.operators.util.OperatorDescriptorUtils;
import scala.Option;
import scala.collection.immutable.Map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static scala.jdk.javaapi.CollectionConverters.asScala;


public class PythonUDFSourceOpDescV2 extends SourceOperatorDescriptor {

    @JsonProperty(required = true, defaultValue =
            "# Choose from the following templates:\n" +
                    "# \n" +
                    "# from pytexera import *\n" +
                    "# \n" +
                    "# class GenerateOperator(UDFSourceOperator):\n" +
                    "# \n" +
                    "#     @overrides\n" +
                    "#     def produce(self) -> Iterator[Union[TupleLike, TableLike, None]]:\n" +
                    "#         yield\n")
    @JsonSchemaTitle("Python script")
    @JsonPropertyDescription("Input your code here")
    public String code;

    @JsonProperty(required = true, defaultValue = "1")
    @JsonSchemaTitle("Worker count")
    @JsonPropertyDescription("Specify how many parallel workers to lunch")
    public Integer workers = 1;

    @JsonProperty()
    @JsonSchemaTitle("Columns")
    @JsonPropertyDescription("The columns of the source")
    public List<Attribute> columns;

    @Override
    public PhysicalOp getPhysicalOp(WorkflowIdentity workflowId, ExecutionIdentity executionId) {
        OpExecInitInfo exec = OpExecInitInfo.apply(code, "python");
        Preconditions.checkArgument(workers >= 1, "Need at least 1 worker.");
        SchemaPropagationFunc func = SchemaPropagationFunc.apply((Function<Map<PortIdentity, Schema>, Map<PortIdentity, Schema>> & Serializable) inputSchemas -> {
            // Initialize a Java HashMap
            java.util.Map<PortIdentity, Schema> javaMap = new java.util.HashMap<>();

            javaMap.put(operatorInfo().outputPorts().head().id(), sourceSchema());

            // Convert the Java Map to a Scala immutable Map
            return OperatorDescriptorUtils.toImmutableMap(javaMap);
        });
        PhysicalOp physicalOp = PhysicalOp.sourcePhysicalOp(
                        workflowId,
                        executionId,
                        operatorIdentifier(),
                        exec
                )
                .withInputPorts(operatorInfo().inputPorts())
                .withOutputPorts(operatorInfo().outputPorts())
                .withIsOneToManyOp(true)
                .withPropagateSchema(func)
                .withLocationPreference(Option.empty());


        if (workers > 1) {
            return physicalOp
                    .withParallelizable(true)
                    .withSuggestedWorkerNum(workers);
        } else {
            return physicalOp.withParallelizable(false);
        }

    }

    @Override
    public OperatorInfo operatorInfo() {
        return new OperatorInfo(
                "1-out Python UDF",
                "User-defined function operator in Python script",
                OperatorGroupConstants.PYTHON_GROUP(),
                asScala(new ArrayList<InputPort>()).toList(),
                asScala(singletonList(new OutputPort(new PortIdentity(0, false), "", false, ""))).toList(),
                false,
                false,
                true,
                false
        );
    }

    @Override
    public Schema sourceSchema() {
        Schema.Builder outputSchemaBuilder = Schema.builder();

        // for any pythonUDFType, it can add custom output columns (attributes).
        if (columns != null) {
            outputSchemaBuilder.add(asScala(columns)).build();
        }
        return outputSchemaBuilder.build();
    }
}
