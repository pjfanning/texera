/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.model.jooq.generated.tables;


import edu.uci.ics.texera.model.jooq.generated.Indexes;
import edu.uci.ics.texera.model.jooq.generated.Keys;
import edu.uci.ics.texera.model.jooq.generated.TexeraDb;
import edu.uci.ics.texera.model.jooq.generated.tables.records.WorkflowExecutionsRecord;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row11;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class WorkflowExecutions extends TableImpl<WorkflowExecutionsRecord> {

    private static final long serialVersionUID = 705215547;

    /**
     * The reference instance of <code>texera_db.workflow_executions</code>
     */
    public static final WorkflowExecutions WORKFLOW_EXECUTIONS = new WorkflowExecutions();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<WorkflowExecutionsRecord> getRecordType() {
        return WorkflowExecutionsRecord.class;
    }

    /**
     * The column <code>texera_db.workflow_executions.eid</code>.
     */
    public final TableField<WorkflowExecutionsRecord, UInteger> EID = createField(DSL.name("eid"), org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false).identity(true), this, "");

    /**
     * The column <code>texera_db.workflow_executions.vid</code>.
     */
    public final TableField<WorkflowExecutionsRecord, UInteger> VID = createField(DSL.name("vid"), org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>texera_db.workflow_executions.uid</code>.
     */
    public final TableField<WorkflowExecutionsRecord, UInteger> UID = createField(DSL.name("uid"), org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>texera_db.workflow_executions.status</code>.
     */
    public final TableField<WorkflowExecutionsRecord, Byte> STATUS = createField(DSL.name("status"), org.jooq.impl.SQLDataType.TINYINT.nullable(false).defaultValue(org.jooq.impl.DSL.inline("1", org.jooq.impl.SQLDataType.TINYINT)), this, "");

    /**
     * The column <code>texera_db.workflow_executions.result</code>.
     */
    public final TableField<WorkflowExecutionsRecord, String> RESULT = createField(DSL.name("result"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>texera_db.workflow_executions.starting_time</code>.
     */
    public final TableField<WorkflowExecutionsRecord, Timestamp> STARTING_TIME = createField(DSL.name("starting_time"), org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");

    /**
     * The column <code>texera_db.workflow_executions.last_update_time</code>.
     */
    public final TableField<WorkflowExecutionsRecord, Timestamp> LAST_UPDATE_TIME = createField(DSL.name("last_update_time"), org.jooq.impl.SQLDataType.TIMESTAMP, this, "");

    /**
     * The column <code>texera_db.workflow_executions.bookmarked</code>.
     */
    public final TableField<WorkflowExecutionsRecord, Byte> BOOKMARKED = createField(DSL.name("bookmarked"), org.jooq.impl.SQLDataType.TINYINT.defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.TINYINT)), this, "");

    /**
     * The column <code>texera_db.workflow_executions.name</code>.
     */
    public final TableField<WorkflowExecutionsRecord, String> NAME = createField(DSL.name("name"), org.jooq.impl.SQLDataType.VARCHAR(128).nullable(false).defaultValue(org.jooq.impl.DSL.inline("Untitled Execution", org.jooq.impl.SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>texera_db.workflow_executions.environment_version</code>.
     */
    public final TableField<WorkflowExecutionsRecord, String> ENVIRONMENT_VERSION = createField(DSL.name("environment_version"), org.jooq.impl.SQLDataType.VARCHAR(128).nullable(false), this, "");

    /**
     * The column <code>texera_db.workflow_executions.log_location</code>.
     */
    public final TableField<WorkflowExecutionsRecord, String> LOG_LOCATION = createField(DSL.name("log_location"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>texera_db.workflow_executions</code> table reference
     */
    public WorkflowExecutions() {
        this(DSL.name("workflow_executions"), null);
    }

    /**
     * Create an aliased <code>texera_db.workflow_executions</code> table reference
     */
    public WorkflowExecutions(String alias) {
        this(DSL.name(alias), WORKFLOW_EXECUTIONS);
    }

    /**
     * Create an aliased <code>texera_db.workflow_executions</code> table reference
     */
    public WorkflowExecutions(Name alias) {
        this(alias, WORKFLOW_EXECUTIONS);
    }

    private WorkflowExecutions(Name alias, Table<WorkflowExecutionsRecord> aliased) {
        this(alias, aliased, null);
    }

    private WorkflowExecutions(Name alias, Table<WorkflowExecutionsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> WorkflowExecutions(Table<O> child, ForeignKey<O, WorkflowExecutionsRecord> key) {
        super(child, key, WORKFLOW_EXECUTIONS);
    }

    @Override
    public Schema getSchema() {
        return TexeraDb.TEXERA_DB;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.WORKFLOW_EXECUTIONS_PRIMARY, Indexes.WORKFLOW_EXECUTIONS_UID, Indexes.WORKFLOW_EXECUTIONS_VID);
    }

    @Override
    public Identity<WorkflowExecutionsRecord, UInteger> getIdentity() {
        return Keys.IDENTITY_WORKFLOW_EXECUTIONS;
    }

    @Override
    public UniqueKey<WorkflowExecutionsRecord> getPrimaryKey() {
        return Keys.KEY_WORKFLOW_EXECUTIONS_PRIMARY;
    }

    @Override
    public List<UniqueKey<WorkflowExecutionsRecord>> getKeys() {
        return Arrays.<UniqueKey<WorkflowExecutionsRecord>>asList(Keys.KEY_WORKFLOW_EXECUTIONS_PRIMARY);
    }

    @Override
    public List<ForeignKey<WorkflowExecutionsRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<WorkflowExecutionsRecord, ?>>asList(Keys.WORKFLOW_EXECUTIONS_IBFK_1, Keys.WORKFLOW_EXECUTIONS_IBFK_2);
    }

    public WorkflowVersion workflowVersion() {
        return new WorkflowVersion(this, Keys.WORKFLOW_EXECUTIONS_IBFK_1);
    }

    public User user() {
        return new User(this, Keys.WORKFLOW_EXECUTIONS_IBFK_2);
    }

    @Override
    public WorkflowExecutions as(String alias) {
        return new WorkflowExecutions(DSL.name(alias), this);
    }

    @Override
    public WorkflowExecutions as(Name alias) {
        return new WorkflowExecutions(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public WorkflowExecutions rename(String name) {
        return new WorkflowExecutions(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public WorkflowExecutions rename(Name name) {
        return new WorkflowExecutions(name, null);
    }

    // -------------------------------------------------------------------------
    // Row11 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row11<UInteger, UInteger, UInteger, Byte, String, Timestamp, Timestamp, Byte, String, String, String> fieldsRow() {
        return (Row11) super.fieldsRow();
    }
}
