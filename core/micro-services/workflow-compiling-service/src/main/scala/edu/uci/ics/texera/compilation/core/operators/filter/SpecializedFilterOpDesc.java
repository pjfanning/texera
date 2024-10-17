package edu.uci.ics.texera.compilation.core.operators.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import edu.uci.ics.amber.core.executor.OpExecInitInfo;
import edu.uci.ics.amber.core.workflow.PhysicalOp;
import edu.uci.ics.amber.virtualidentity.ExecutionIdentity;
import edu.uci.ics.amber.virtualidentity.WorkflowIdentity;
import edu.uci.ics.amber.workflow.InputPort;
import edu.uci.ics.amber.workflow.OutputPort;
import edu.uci.ics.amber.workflow.PortIdentity;
import edu.uci.ics.texera.compilation.core.common.executor.OperatorExecutor;
import edu.uci.ics.texera.compilation.core.common.metadata.OperatorGroupConstants;
import edu.uci.ics.texera.compilation.core.common.metadata.OperatorInfo;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static scala.jdk.javaapi.CollectionConverters.asScala;

public class SpecializedFilterOpDesc extends FilterOpDesc {

    @JsonProperty(value = "predicates", required = true)
    @JsonPropertyDescription("multiple predicates in OR")
    public java.util.List<FilterPredicate> predicates;

    @Override
    public PhysicalOp getPhysicalOp(WorkflowIdentity workflowId, ExecutionIdentity executionId) {
        return PhysicalOp.oneToOnePhysicalOp(
                        workflowId,
                        executionId,
                        operatorIdentifier(),
                        OpExecInitInfo.apply(
                                (Function<Tuple2<Object, Object>, OperatorExecutor> & java.io.Serializable)
                                        x -> new SpecializedFilterOpExec(this.predicates)
                        )
                )
                .withInputPorts(operatorInfo().inputPorts())
                .withOutputPorts(operatorInfo().outputPorts());
    }

    @Override
    public OperatorInfo operatorInfo() {
        return new OperatorInfo(
                "Filter",
                "Performs a filter operation",
                OperatorGroupConstants.CLEANING_GROUP(),
                asScala(singletonList(new InputPort(new PortIdentity(0, false), "", false, asScala(new ArrayList<PortIdentity>()).toSeq()))).toList(),
                asScala(singletonList(new OutputPort(new PortIdentity(0, false), "", false))).toList(),
                false,
                false,
                true,
                false
        );
    }
}
