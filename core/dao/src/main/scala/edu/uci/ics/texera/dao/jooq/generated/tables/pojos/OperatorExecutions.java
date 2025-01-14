/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.dao.jooq.generated.tables.pojos;


import edu.uci.ics.texera.dao.jooq.generated.tables.interfaces.IOperatorExecutions;

import org.jooq.types.UInteger;
import org.jooq.types.ULong;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OperatorExecutions implements IOperatorExecutions {

    private static final long serialVersionUID = -1568831818;

    private ULong    operatorExecutionId;
    private UInteger workflowExecutionId;
    private String   operatorId;

    public OperatorExecutions() {}

    public OperatorExecutions(IOperatorExecutions value) {
        this.operatorExecutionId = value.getOperatorExecutionId();
        this.workflowExecutionId = value.getWorkflowExecutionId();
        this.operatorId = value.getOperatorId();
    }

    public OperatorExecutions(
        ULong    operatorExecutionId,
        UInteger workflowExecutionId,
        String   operatorId
    ) {
        this.operatorExecutionId = operatorExecutionId;
        this.workflowExecutionId = workflowExecutionId;
        this.operatorId = operatorId;
    }

    @Override
    public ULong getOperatorExecutionId() {
        return this.operatorExecutionId;
    }

    @Override
    public void setOperatorExecutionId(ULong operatorExecutionId) {
        this.operatorExecutionId = operatorExecutionId;
    }

    @Override
    public UInteger getWorkflowExecutionId() {
        return this.workflowExecutionId;
    }

    @Override
    public void setWorkflowExecutionId(UInteger workflowExecutionId) {
        this.workflowExecutionId = workflowExecutionId;
    }

    @Override
    public String getOperatorId() {
        return this.operatorId;
    }

    @Override
    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("OperatorExecutions (");

        sb.append(operatorExecutionId);
        sb.append(", ").append(workflowExecutionId);
        sb.append(", ").append(operatorId);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IOperatorExecutions from) {
        setOperatorExecutionId(from.getOperatorExecutionId());
        setWorkflowExecutionId(from.getWorkflowExecutionId());
        setOperatorId(from.getOperatorId());
    }

    @Override
    public <E extends IOperatorExecutions> E into(E into) {
        into.from(this);
        return into;
    }
}
