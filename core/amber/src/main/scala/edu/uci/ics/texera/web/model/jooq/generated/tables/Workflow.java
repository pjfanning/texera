/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.web.model.jooq.generated.tables;


import edu.uci.ics.texera.web.model.jooq.generated.Indexes;
import edu.uci.ics.texera.web.model.jooq.generated.Keys;
import edu.uci.ics.texera.web.model.jooq.generated.TexeraDb;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.WorkflowRecord;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row7;
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
public class Workflow extends TableImpl<WorkflowRecord> {

    private static final long serialVersionUID = -1216485099;

    /**
     * The reference instance of <code>texera_db.workflow</code>
     */
    public static final Workflow WORKFLOW = new Workflow();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<WorkflowRecord> getRecordType() {
        return WorkflowRecord.class;
    }

    /**
     * The column <code>texera_db.workflow.name</code>.
     */
    public final TableField<WorkflowRecord, String> NAME = createField(DSL.name("name"), org.jooq.impl.SQLDataType.VARCHAR(128).nullable(false), this, "");

    /**
     * The column <code>texera_db.workflow.description</code>.
     */
    public final TableField<WorkflowRecord, String> DESCRIPTION = createField(DSL.name("description"), org.jooq.impl.SQLDataType.VARCHAR(500), this, "");

    /**
     * The column <code>texera_db.workflow.wid</code>.
     */
    public final TableField<WorkflowRecord, UInteger> WID = createField(DSL.name("wid"), org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false).identity(true), this, "");

    /**
     * The column <code>texera_db.workflow.content</code>.
     */
    public final TableField<WorkflowRecord, String> CONTENT = createField(DSL.name("content"), org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>texera_db.workflow.creation_time</code>.
     */
    public final TableField<WorkflowRecord, Timestamp> CREATION_TIME = createField(DSL.name("creation_time"), org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");

    /**
     * The column <code>texera_db.workflow.last_modified_time</code>.
     */
    public final TableField<WorkflowRecord, Timestamp> LAST_MODIFIED_TIME = createField(DSL.name("last_modified_time"), org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");

    /**
     * The column <code>texera_db.workflow.eid</code>.
     */
    public final TableField<WorkflowRecord, UInteger> EID = createField(DSL.name("eid"), org.jooq.impl.SQLDataType.INTEGERUNSIGNED, this, "");

    /**
     * Create a <code>texera_db.workflow</code> table reference
     */
    public Workflow() {
        this(DSL.name("workflow"), null);
    }

    /**
     * Create an aliased <code>texera_db.workflow</code> table reference
     */
    public Workflow(String alias) {
        this(DSL.name(alias), WORKFLOW);
    }

    /**
     * Create an aliased <code>texera_db.workflow</code> table reference
     */
    public Workflow(Name alias) {
        this(alias, WORKFLOW);
    }

    private Workflow(Name alias, Table<WorkflowRecord> aliased) {
        this(alias, aliased, null);
    }

    private Workflow(Name alias, Table<WorkflowRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> Workflow(Table<O> child, ForeignKey<O, WorkflowRecord> key) {
        super(child, key, WORKFLOW);
    }

    @Override
    public Schema getSchema() {
        return TexeraDb.TEXERA_DB;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.WORKFLOW_FK_WORKFLOW_ENVIRONMENT, Indexes.WORKFLOW_IDX_WORKFLOW_NAME_DESCRIPTION_CONTENT, Indexes.WORKFLOW_PRIMARY);
    }

    @Override
    public Identity<WorkflowRecord, UInteger> getIdentity() {
        return Keys.IDENTITY_WORKFLOW;
    }

    @Override
    public UniqueKey<WorkflowRecord> getPrimaryKey() {
        return Keys.KEY_WORKFLOW_PRIMARY;
    }

    @Override
    public List<UniqueKey<WorkflowRecord>> getKeys() {
        return Arrays.<UniqueKey<WorkflowRecord>>asList(Keys.KEY_WORKFLOW_PRIMARY);
    }

    @Override
    public List<ForeignKey<WorkflowRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<WorkflowRecord, ?>>asList(Keys.FK_WORKFLOW_ENVIRONMENT);
    }

    public Environment environment() {
        return new Environment(this, Keys.FK_WORKFLOW_ENVIRONMENT);
    }

    @Override
    public Workflow as(String alias) {
        return new Workflow(DSL.name(alias), this);
    }

    @Override
    public Workflow as(Name alias) {
        return new Workflow(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Workflow rename(String name) {
        return new Workflow(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Workflow rename(Name name) {
        return new Workflow(name, null);
    }

    // -------------------------------------------------------------------------
    // Row7 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row7<String, String, UInteger, String, Timestamp, Timestamp, UInteger> fieldsRow() {
        return (Row7) super.fieldsRow();
    }
}
