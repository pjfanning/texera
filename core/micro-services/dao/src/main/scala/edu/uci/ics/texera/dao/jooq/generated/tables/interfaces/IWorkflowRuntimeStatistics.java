/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.dao.jooq.generated.tables.interfaces;


import java.io.Serializable;
import java.sql.Timestamp;

import org.jooq.types.UInteger;
import org.jooq.types.ULong;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IWorkflowRuntimeStatistics extends Serializable {

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.workflow_id</code>.
     */
    public void setWorkflowId(UInteger value);

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.workflow_id</code>.
     */
    public UInteger getWorkflowId();

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.execution_id</code>.
     */
    public void setExecutionId(UInteger value);

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.execution_id</code>.
     */
    public UInteger getExecutionId();

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.operator_id</code>.
     */
    public void setOperatorId(String value);

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.operator_id</code>.
     */
    public String getOperatorId();

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.time</code>.
     */
    public void setTime(Timestamp value);

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.time</code>.
     */
    public Timestamp getTime();

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.input_tuple_cnt</code>.
     */
    public void setInputTupleCnt(UInteger value);

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.input_tuple_cnt</code>.
     */
    public UInteger getInputTupleCnt();

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.output_tuple_cnt</code>.
     */
    public void setOutputTupleCnt(UInteger value);

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.output_tuple_cnt</code>.
     */
    public UInteger getOutputTupleCnt();

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.status</code>.
     */
    public void setStatus(Byte value);

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.status</code>.
     */
    public Byte getStatus();

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.data_processing_time</code>.
     */
    public void setDataProcessingTime(ULong value);

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.data_processing_time</code>.
     */
    public ULong getDataProcessingTime();

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.control_processing_time</code>.
     */
    public void setControlProcessingTime(ULong value);

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.control_processing_time</code>.
     */
    public ULong getControlProcessingTime();

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.idle_time</code>.
     */
    public void setIdleTime(ULong value);

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.idle_time</code>.
     */
    public ULong getIdleTime();

    /**
     * Setter for <code>texera_db.workflow_runtime_statistics.num_workers</code>.
     */
    public void setNumWorkers(UInteger value);

    /**
     * Getter for <code>texera_db.workflow_runtime_statistics.num_workers</code>.
     */
    public UInteger getNumWorkers();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface IWorkflowRuntimeStatistics
     */
    public void from(edu.uci.ics.texera.dao.jooq.generated.tables.interfaces.IWorkflowRuntimeStatistics from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface IWorkflowRuntimeStatistics
     */
    public <E extends edu.uci.ics.texera.dao.jooq.generated.tables.interfaces.IWorkflowRuntimeStatistics> E into(E into);
}
