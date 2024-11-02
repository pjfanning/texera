/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.dao.jooq.generated.tables.records;


import edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowRuntimeStatistics;
import edu.uci.ics.texera.dao.jooq.generated.tables.interfaces.IWorkflowRuntimeStatistics;

import java.sql.Timestamp;

import org.jooq.Field;
import org.jooq.Record11;
import org.jooq.Record4;
import org.jooq.Row11;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class WorkflowRuntimeStatisticsRecord extends UpdatableRecordImpl<WorkflowRuntimeStatisticsRecord> implements Record11<UInteger, UInteger, String, Timestamp, UInteger, UInteger, Byte, ULong, ULong, ULong, UInteger>, IWorkflowRuntimeStatistics {

    private static final long serialVersionUID = 101870722;

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.workflow_id</code>.
     */
    @Override
    public void setWorkflowId(UInteger value) {
        set(0, value);
    }

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.workflow_id</code>.
     */
    @Override
    public UInteger getWorkflowId() {
        return (UInteger) get(0);
    }

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.execution_id</code>.
     */
    @Override
    public void setExecutionId(UInteger value) {
        set(1, value);
    }

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.execution_id</code>.
     */
    @Override
    public UInteger getExecutionId() {
        return (UInteger) get(1);
    }

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.operator_id</code>.
     */
    @Override
    public void setOperatorId(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.operator_id</code>.
     */
    @Override
    public String getOperatorId() {
        return (String) get(2);
    }

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.time</code>.
     */
    @Override
    public void setTime(Timestamp value) {
        set(3, value);
    }

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.time</code>.
     */
    @Override
    public Timestamp getTime() {
        return (Timestamp) get(3);
    }

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.input_tuple_cnt</code>.
     */
    @Override
    public void setInputTupleCnt(UInteger value) {
        set(4, value);
    }

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.input_tuple_cnt</code>.
     */
    @Override
    public UInteger getInputTupleCnt() {
        return (UInteger) get(4);
    }

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.output_tuple_cnt</code>.
     */
    @Override
    public void setOutputTupleCnt(UInteger value) {
        set(5, value);
    }

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.output_tuple_cnt</code>.
     */
    @Override
    public UInteger getOutputTupleCnt() {
        return (UInteger) get(5);
    }

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.status</code>.
     */
    @Override
    public void setStatus(Byte value) {
        set(6, value);
    }

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.status</code>.
     */
    @Override
    public Byte getStatus() {
        return (Byte) get(6);
    }

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.data_processing_time</code>.
     */
    @Override
    public void setDataProcessingTime(ULong value) {
        set(7, value);
    }

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.data_processing_time</code>.
     */
    @Override
    public ULong getDataProcessingTime() {
        return (ULong) get(7);
    }

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.control_processing_time</code>.
     */
    @Override
    public void setControlProcessingTime(ULong value) {
        set(8, value);
    }

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.control_processing_time</code>.
     */
    @Override
    public ULong getControlProcessingTime() {
        return (ULong) get(8);
    }

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.idle_time</code>.
     */
    @Override
    public void setIdleTime(ULong value) {
        set(9, value);
    }

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.idle_time</code>.
     */
    @Override
    public ULong getIdleTime() {
        return (ULong) get(9);
    }

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.num_workers</code>.
     */
    @Override
    public void setNumWorkers(UInteger value) {
        set(10, value);
    }

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.num_workers</code>.
     */
    @Override
    public UInteger getNumWorkers() {
        return (UInteger) get(10);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record4<UInteger, UInteger, String, Timestamp> key() {
        return (Record4) super.key();
    }

    // -------------------------------------------------------------------------
    // Record11 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row11<UInteger, UInteger, String, Timestamp, UInteger, UInteger, Byte, ULong, ULong, ULong, UInteger> fieldsRow() {
        return (Row11) super.fieldsRow();
    }

    @Override
    public Row11<UInteger, UInteger, String, Timestamp, UInteger, UInteger, Byte, ULong, ULong, ULong, UInteger> valuesRow() {
        return (Row11) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.WORKFLOW_ID;
    }

    @Override
    public Field<UInteger> field2() {
        return WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.EXECUTION_ID;
    }

    @Override
    public Field<String> field3() {
        return WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.OPERATOR_ID;
    }

    @Override
    public Field<Timestamp> field4() {
        return WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.TIME;
    }

    @Override
    public Field<UInteger> field5() {
        return WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.INPUT_TUPLE_CNT;
    }

    @Override
    public Field<UInteger> field6() {
        return WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.OUTPUT_TUPLE_CNT;
    }

    @Override
    public Field<Byte> field7() {
        return WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.STATUS;
    }

    @Override
    public Field<ULong> field8() {
        return WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.DATA_PROCESSING_TIME;
    }

    @Override
    public Field<ULong> field9() {
        return WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.CONTROL_PROCESSING_TIME;
    }

    @Override
    public Field<ULong> field10() {
        return WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.IDLE_TIME;
    }

    @Override
    public Field<UInteger> field11() {
        return WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS.NUM_WORKERS;
    }

    @Override
    public UInteger component1() {
        return getWorkflowId();
    }

    @Override
    public UInteger component2() {
        return getExecutionId();
    }

    @Override
    public String component3() {
        return getOperatorId();
    }

    @Override
    public Timestamp component4() {
        return getTime();
    }

    @Override
    public UInteger component5() {
        return getInputTupleCnt();
    }

    @Override
    public UInteger component6() {
        return getOutputTupleCnt();
    }

    @Override
    public Byte component7() {
        return getStatus();
    }

    @Override
    public ULong component8() {
        return getDataProcessingTime();
    }

    @Override
    public ULong component9() {
        return getControlProcessingTime();
    }

    @Override
    public ULong component10() {
        return getIdleTime();
    }

    @Override
    public UInteger component11() {
        return getNumWorkers();
    }

    @Override
    public UInteger value1() {
        return getWorkflowId();
    }

    @Override
    public UInteger value2() {
        return getExecutionId();
    }

    @Override
    public String value3() {
        return getOperatorId();
    }

    @Override
    public Timestamp value4() {
        return getTime();
    }

    @Override
    public UInteger value5() {
        return getInputTupleCnt();
    }

    @Override
    public UInteger value6() {
        return getOutputTupleCnt();
    }

    @Override
    public Byte value7() {
        return getStatus();
    }

    @Override
    public ULong value8() {
        return getDataProcessingTime();
    }

    @Override
    public ULong value9() {
        return getControlProcessingTime();
    }

    @Override
    public ULong value10() {
        return getIdleTime();
    }

    @Override
    public UInteger value11() {
        return getNumWorkers();
    }

    @Override
    public WorkflowRuntimeStatisticsRecord value1(UInteger value) {
        setWorkflowId(value);
        return this;
    }

    @Override
    public WorkflowRuntimeStatisticsRecord value2(UInteger value) {
        setExecutionId(value);
        return this;
    }

    @Override
    public WorkflowRuntimeStatisticsRecord value3(String value) {
        setOperatorId(value);
        return this;
    }

    @Override
    public WorkflowRuntimeStatisticsRecord value4(Timestamp value) {
        setTime(value);
        return this;
    }

    @Override
    public WorkflowRuntimeStatisticsRecord value5(UInteger value) {
        setInputTupleCnt(value);
        return this;
    }

    @Override
    public WorkflowRuntimeStatisticsRecord value6(UInteger value) {
        setOutputTupleCnt(value);
        return this;
    }

    @Override
    public WorkflowRuntimeStatisticsRecord value7(Byte value) {
        setStatus(value);
        return this;
    }

    @Override
    public WorkflowRuntimeStatisticsRecord value8(ULong value) {
        setDataProcessingTime(value);
        return this;
    }

    @Override
    public WorkflowRuntimeStatisticsRecord value9(ULong value) {
        setControlProcessingTime(value);
        return this;
    }

    @Override
    public WorkflowRuntimeStatisticsRecord value10(ULong value) {
        setIdleTime(value);
        return this;
    }

    @Override
    public WorkflowRuntimeStatisticsRecord value11(UInteger value) {
        setNumWorkers(value);
        return this;
    }

    @Override
    public WorkflowRuntimeStatisticsRecord values(UInteger value1, UInteger value2, String value3, Timestamp value4, UInteger value5, UInteger value6, Byte value7, ULong value8, ULong value9, ULong value10, UInteger value11) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        value10(value10);
        value11(value11);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IWorkflowRuntimeStatistics from) {
        setWorkflowId(from.getWorkflowId());
        setExecutionId(from.getExecutionId());
        setOperatorId(from.getOperatorId());
        setTime(from.getTime());
        setInputTupleCnt(from.getInputTupleCnt());
        setOutputTupleCnt(from.getOutputTupleCnt());
        setStatus(from.getStatus());
        setDataProcessingTime(from.getDataProcessingTime());
        setControlProcessingTime(from.getControlProcessingTime());
        setIdleTime(from.getIdleTime());
        setNumWorkers(from.getNumWorkers());
    }

    @Override
    public <E extends IWorkflowRuntimeStatistics> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached WorkflowRuntimeStatisticsRecord
     */
    public WorkflowRuntimeStatisticsRecord() {
        super(WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS);
    }

    /**
     * Create a detached, initialised WorkflowRuntimeStatisticsRecord
     */
    public WorkflowRuntimeStatisticsRecord(UInteger workflowId, UInteger executionId, String operatorId, Timestamp time, UInteger inputTupleCnt, UInteger outputTupleCnt, Byte status, ULong dataProcessingTime, ULong controlProcessingTime, ULong idleTime, UInteger numWorkers) {
        super(WorkflowRuntimeStatistics.WORKFLOW_RUNTIME_STATISTICS);

        set(0, workflowId);
        set(1, executionId);
        set(2, operatorId);
        set(3, time);
        set(4, inputTupleCnt);
        set(5, outputTupleCnt);
        set(6, status);
        set(7, dataProcessingTime);
        set(8, controlProcessingTime);
        set(9, idleTime);
        set(10, numWorkers);
    }
}
