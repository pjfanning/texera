/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.model.jooq.generated.tables.records;


import edu.uci.ics.texera.model.jooq.generated.tables.WorkflowExecutions;
import edu.uci.ics.texera.model.jooq.generated.tables.interfaces.IWorkflowExecutions;

import java.sql.Timestamp;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record11;
import org.jooq.Row11;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class WorkflowExecutionsRecord extends UpdatableRecordImpl<WorkflowExecutionsRecord> implements Record11<UInteger, UInteger, UInteger, Byte, String, Timestamp, Timestamp, Byte, String, String, String>, IWorkflowExecutions {

    private static final long serialVersionUID = 403212134;

    /**
     * Setter for <code>texera_db.workflow_executions.eid</code>.
     */
    @Override
    public void setEid(UInteger value) {
        set(0, value);
    }

    /**
     * Getter for <code>texera_db.workflow_executions.eid</code>.
     */
    @Override
    public UInteger getEid() {
        return (UInteger) get(0);
    }

    /**
     * Setter for <code>texera_db.workflow_executions.vid</code>.
     */
    @Override
    public void setVid(UInteger value) {
        set(1, value);
    }

    /**
     * Getter for <code>texera_db.workflow_executions.vid</code>.
     */
    @Override
    public UInteger getVid() {
        return (UInteger) get(1);
    }

    /**
     * Setter for <code>texera_db.workflow_executions.uid</code>.
     */
    @Override
    public void setUid(UInteger value) {
        set(2, value);
    }

    /**
     * Getter for <code>texera_db.workflow_executions.uid</code>.
     */
    @Override
    public UInteger getUid() {
        return (UInteger) get(2);
    }

    /**
     * Setter for <code>texera_db.workflow_executions.status</code>.
     */
    @Override
    public void setStatus(Byte value) {
        set(3, value);
    }

    /**
     * Getter for <code>texera_db.workflow_executions.status</code>.
     */
    @Override
    public Byte getStatus() {
        return (Byte) get(3);
    }

    /**
     * Setter for <code>texera_db.workflow_executions.result</code>.
     */
    @Override
    public void setResult(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>texera_db.workflow_executions.result</code>.
     */
    @Override
    public String getResult() {
        return (String) get(4);
    }

    /**
     * Setter for <code>texera_db.workflow_executions.starting_time</code>.
     */
    @Override
    public void setStartingTime(Timestamp value) {
        set(5, value);
    }

    /**
     * Getter for <code>texera_db.workflow_executions.starting_time</code>.
     */
    @Override
    public Timestamp getStartingTime() {
        return (Timestamp) get(5);
    }

    /**
     * Setter for <code>texera_db.workflow_executions.last_update_time</code>.
     */
    @Override
    public void setLastUpdateTime(Timestamp value) {
        set(6, value);
    }

    /**
     * Getter for <code>texera_db.workflow_executions.last_update_time</code>.
     */
    @Override
    public Timestamp getLastUpdateTime() {
        return (Timestamp) get(6);
    }

    /**
     * Setter for <code>texera_db.workflow_executions.bookmarked</code>.
     */
    @Override
    public void setBookmarked(Byte value) {
        set(7, value);
    }

    /**
     * Getter for <code>texera_db.workflow_executions.bookmarked</code>.
     */
    @Override
    public Byte getBookmarked() {
        return (Byte) get(7);
    }

    /**
     * Setter for <code>texera_db.workflow_executions.name</code>.
     */
    @Override
    public void setName(String value) {
        set(8, value);
    }

    /**
     * Getter for <code>texera_db.workflow_executions.name</code>.
     */
    @Override
    public String getName() {
        return (String) get(8);
    }

    /**
     * Setter for <code>texera_db.workflow_executions.environment_version</code>.
     */
    @Override
    public void setEnvironmentVersion(String value) {
        set(9, value);
    }

    /**
     * Getter for <code>texera_db.workflow_executions.environment_version</code>.
     */
    @Override
    public String getEnvironmentVersion() {
        return (String) get(9);
    }

    /**
     * Setter for <code>texera_db.workflow_executions.log_location</code>.
     */
    @Override
    public void setLogLocation(String value) {
        set(10, value);
    }

    /**
     * Getter for <code>texera_db.workflow_executions.log_location</code>.
     */
    @Override
    public String getLogLocation() {
        return (String) get(10);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record11 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row11<UInteger, UInteger, UInteger, Byte, String, Timestamp, Timestamp, Byte, String, String, String> fieldsRow() {
        return (Row11) super.fieldsRow();
    }

    @Override
    public Row11<UInteger, UInteger, UInteger, Byte, String, Timestamp, Timestamp, Byte, String, String, String> valuesRow() {
        return (Row11) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return WorkflowExecutions.WORKFLOW_EXECUTIONS.EID;
    }

    @Override
    public Field<UInteger> field2() {
        return WorkflowExecutions.WORKFLOW_EXECUTIONS.VID;
    }

    @Override
    public Field<UInteger> field3() {
        return WorkflowExecutions.WORKFLOW_EXECUTIONS.UID;
    }

    @Override
    public Field<Byte> field4() {
        return WorkflowExecutions.WORKFLOW_EXECUTIONS.STATUS;
    }

    @Override
    public Field<String> field5() {
        return WorkflowExecutions.WORKFLOW_EXECUTIONS.RESULT;
    }

    @Override
    public Field<Timestamp> field6() {
        return WorkflowExecutions.WORKFLOW_EXECUTIONS.STARTING_TIME;
    }

    @Override
    public Field<Timestamp> field7() {
        return WorkflowExecutions.WORKFLOW_EXECUTIONS.LAST_UPDATE_TIME;
    }

    @Override
    public Field<Byte> field8() {
        return WorkflowExecutions.WORKFLOW_EXECUTIONS.BOOKMARKED;
    }

    @Override
    public Field<String> field9() {
        return WorkflowExecutions.WORKFLOW_EXECUTIONS.NAME;
    }

    @Override
    public Field<String> field10() {
        return WorkflowExecutions.WORKFLOW_EXECUTIONS.ENVIRONMENT_VERSION;
    }

    @Override
    public Field<String> field11() {
        return WorkflowExecutions.WORKFLOW_EXECUTIONS.LOG_LOCATION;
    }

    @Override
    public UInteger component1() {
        return getEid();
    }

    @Override
    public UInteger component2() {
        return getVid();
    }

    @Override
    public UInteger component3() {
        return getUid();
    }

    @Override
    public Byte component4() {
        return getStatus();
    }

    @Override
    public String component5() {
        return getResult();
    }

    @Override
    public Timestamp component6() {
        return getStartingTime();
    }

    @Override
    public Timestamp component7() {
        return getLastUpdateTime();
    }

    @Override
    public Byte component8() {
        return getBookmarked();
    }

    @Override
    public String component9() {
        return getName();
    }

    @Override
    public String component10() {
        return getEnvironmentVersion();
    }

    @Override
    public String component11() {
        return getLogLocation();
    }

    @Override
    public UInteger value1() {
        return getEid();
    }

    @Override
    public UInteger value2() {
        return getVid();
    }

    @Override
    public UInteger value3() {
        return getUid();
    }

    @Override
    public Byte value4() {
        return getStatus();
    }

    @Override
    public String value5() {
        return getResult();
    }

    @Override
    public Timestamp value6() {
        return getStartingTime();
    }

    @Override
    public Timestamp value7() {
        return getLastUpdateTime();
    }

    @Override
    public Byte value8() {
        return getBookmarked();
    }

    @Override
    public String value9() {
        return getName();
    }

    @Override
    public String value10() {
        return getEnvironmentVersion();
    }

    @Override
    public String value11() {
        return getLogLocation();
    }

    @Override
    public WorkflowExecutionsRecord value1(UInteger value) {
        setEid(value);
        return this;
    }

    @Override
    public WorkflowExecutionsRecord value2(UInteger value) {
        setVid(value);
        return this;
    }

    @Override
    public WorkflowExecutionsRecord value3(UInteger value) {
        setUid(value);
        return this;
    }

    @Override
    public WorkflowExecutionsRecord value4(Byte value) {
        setStatus(value);
        return this;
    }

    @Override
    public WorkflowExecutionsRecord value5(String value) {
        setResult(value);
        return this;
    }

    @Override
    public WorkflowExecutionsRecord value6(Timestamp value) {
        setStartingTime(value);
        return this;
    }

    @Override
    public WorkflowExecutionsRecord value7(Timestamp value) {
        setLastUpdateTime(value);
        return this;
    }

    @Override
    public WorkflowExecutionsRecord value8(Byte value) {
        setBookmarked(value);
        return this;
    }

    @Override
    public WorkflowExecutionsRecord value9(String value) {
        setName(value);
        return this;
    }

    @Override
    public WorkflowExecutionsRecord value10(String value) {
        setEnvironmentVersion(value);
        return this;
    }

    @Override
    public WorkflowExecutionsRecord value11(String value) {
        setLogLocation(value);
        return this;
    }

    @Override
    public WorkflowExecutionsRecord values(UInteger value1, UInteger value2, UInteger value3, Byte value4, String value5, Timestamp value6, Timestamp value7, Byte value8, String value9, String value10, String value11) {
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
    public void from(IWorkflowExecutions from) {
        setEid(from.getEid());
        setVid(from.getVid());
        setUid(from.getUid());
        setStatus(from.getStatus());
        setResult(from.getResult());
        setStartingTime(from.getStartingTime());
        setLastUpdateTime(from.getLastUpdateTime());
        setBookmarked(from.getBookmarked());
        setName(from.getName());
        setEnvironmentVersion(from.getEnvironmentVersion());
        setLogLocation(from.getLogLocation());
    }

    @Override
    public <E extends IWorkflowExecutions> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached WorkflowExecutionsRecord
     */
    public WorkflowExecutionsRecord() {
        super(WorkflowExecutions.WORKFLOW_EXECUTIONS);
    }

    /**
     * Create a detached, initialised WorkflowExecutionsRecord
     */
    public WorkflowExecutionsRecord(UInteger eid, UInteger vid, UInteger uid, Byte status, String result, Timestamp startingTime, Timestamp lastUpdateTime, Byte bookmarked, String name, String environmentVersion, String logLocation) {
        super(WorkflowExecutions.WORKFLOW_EXECUTIONS);

        set(0, eid);
        set(1, vid);
        set(2, uid);
        set(3, status);
        set(4, result);
        set(5, startingTime);
        set(6, lastUpdateTime);
        set(7, bookmarked);
        set(8, name);
        set(9, environmentVersion);
        set(10, logLocation);
    }
}
