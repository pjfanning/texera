/*
 * This file is generated by jOOQ.
 */
package web.model.jooq.generated.tables;


import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row2;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;

import web.model.jooq.generated.Indexes;
import web.model.jooq.generated.Keys;
import web.model.jooq.generated.TexeraDb;
import web.model.jooq.generated.tables.records.FileOfWorkflowRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class FileOfWorkflow extends TableImpl<FileOfWorkflowRecord> {

    private static final long serialVersionUID = -1790182001;

    /**
     * The reference instance of <code>texera_db.file_of_workflow</code>
     */
    public static final FileOfWorkflow FILE_OF_WORKFLOW = new FileOfWorkflow();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<FileOfWorkflowRecord> getRecordType() {
        return FileOfWorkflowRecord.class;
    }

    /**
     * The column <code>texera_db.file_of_workflow.fid</code>.
     */
    public final TableField<FileOfWorkflowRecord, UInteger> FID = createField(DSL.name("fid"), org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>texera_db.file_of_workflow.wid</code>.
     */
    public final TableField<FileOfWorkflowRecord, UInteger> WID = createField(DSL.name("wid"), org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false), this, "");

    /**
     * Create a <code>texera_db.file_of_workflow</code> table reference
     */
    public FileOfWorkflow() {
        this(DSL.name("file_of_workflow"), null);
    }

    /**
     * Create an aliased <code>texera_db.file_of_workflow</code> table reference
     */
    public FileOfWorkflow(String alias) {
        this(DSL.name(alias), FILE_OF_WORKFLOW);
    }

    /**
     * Create an aliased <code>texera_db.file_of_workflow</code> table reference
     */
    public FileOfWorkflow(Name alias) {
        this(alias, FILE_OF_WORKFLOW);
    }

    private FileOfWorkflow(Name alias, Table<FileOfWorkflowRecord> aliased) {
        this(alias, aliased, null);
    }

    private FileOfWorkflow(Name alias, Table<FileOfWorkflowRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> FileOfWorkflow(Table<O> child, ForeignKey<O, FileOfWorkflowRecord> key) {
        super(child, key, FILE_OF_WORKFLOW);
    }

    @Override
    public Schema getSchema() {
        return TexeraDb.TEXERA_DB;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.FILE_OF_WORKFLOW_PRIMARY, Indexes.FILE_OF_WORKFLOW_WID);
    }

    @Override
    public UniqueKey<FileOfWorkflowRecord> getPrimaryKey() {
        return Keys.KEY_FILE_OF_WORKFLOW_PRIMARY;
    }

    @Override
    public List<UniqueKey<FileOfWorkflowRecord>> getKeys() {
        return Arrays.<UniqueKey<FileOfWorkflowRecord>>asList(Keys.KEY_FILE_OF_WORKFLOW_PRIMARY);
    }

    @Override
    public List<ForeignKey<FileOfWorkflowRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<FileOfWorkflowRecord, ?>>asList(Keys.FILE_OF_WORKFLOW_IBFK_1, Keys.FILE_OF_WORKFLOW_IBFK_2);
    }

    public File file() {
        return new File(this, Keys.FILE_OF_WORKFLOW_IBFK_1);
    }

    public Workflow workflow() {
        return new Workflow(this, Keys.FILE_OF_WORKFLOW_IBFK_2);
    }

    @Override
    public FileOfWorkflow as(String alias) {
        return new FileOfWorkflow(DSL.name(alias), this);
    }

    @Override
    public FileOfWorkflow as(Name alias) {
        return new FileOfWorkflow(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public FileOfWorkflow rename(String name) {
        return new FileOfWorkflow(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public FileOfWorkflow rename(Name name) {
        return new FileOfWorkflow(name, null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<UInteger, UInteger> fieldsRow() {
        return (Row2) super.fieldsRow();
    }
}
