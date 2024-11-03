/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.dao.jooq.generated.tables.records;


import edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowViewCount;
import edu.uci.ics.texera.dao.jooq.generated.tables.interfaces.IWorkflowViewCount;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class WorkflowViewCountRecord extends UpdatableRecordImpl<WorkflowViewCountRecord> implements Record2<UInteger, UInteger>, IWorkflowViewCount {

    private static final long serialVersionUID = 1060055745;

    /**
     * Setter for <code>texera_db.workflow_view_count.wid</code>.
     */
    @Override
    public void setWid(UInteger value) {
        set(0, value);
    }

    /**
     * Getter for <code>texera_db.workflow_view_count.wid</code>.
     */
    @Override
    public UInteger getWid() {
        return (UInteger) get(0);
    }

    /**
     * Setter for <code>texera_db.workflow_view_count.view_count</code>.
     */
    @Override
    public void setViewCount(UInteger value) {
        set(1, value);
    }

    /**
     * Getter for <code>texera_db.workflow_view_count.view_count</code>.
     */
    @Override
    public UInteger getViewCount() {
        return (UInteger) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<UInteger, UInteger> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<UInteger, UInteger> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return WorkflowViewCount.WORKFLOW_VIEW_COUNT.WID;
    }

    @Override
    public Field<UInteger> field2() {
        return WorkflowViewCount.WORKFLOW_VIEW_COUNT.VIEW_COUNT;
    }

    @Override
    public UInteger component1() {
        return getWid();
    }

    @Override
    public UInteger component2() {
        return getViewCount();
    }

    @Override
    public UInteger value1() {
        return getWid();
    }

    @Override
    public UInteger value2() {
        return getViewCount();
    }

    @Override
    public WorkflowViewCountRecord value1(UInteger value) {
        setWid(value);
        return this;
    }

    @Override
    public WorkflowViewCountRecord value2(UInteger value) {
        setViewCount(value);
        return this;
    }

    @Override
    public WorkflowViewCountRecord values(UInteger value1, UInteger value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IWorkflowViewCount from) {
        setWid(from.getWid());
        setViewCount(from.getViewCount());
    }

    @Override
    public <E extends IWorkflowViewCount> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached WorkflowViewCountRecord
     */
    public WorkflowViewCountRecord() {
        super(WorkflowViewCount.WORKFLOW_VIEW_COUNT);
    }

    /**
     * Create a detached, initialised WorkflowViewCountRecord
     */
    public WorkflowViewCountRecord(UInteger wid, UInteger viewCount) {
        super(WorkflowViewCount.WORKFLOW_VIEW_COUNT);

        set(0, wid);
        set(1, viewCount);
    }
}
