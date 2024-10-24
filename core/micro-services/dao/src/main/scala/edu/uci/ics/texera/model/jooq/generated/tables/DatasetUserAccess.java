/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.model.jooq.generated.tables;


import edu.uci.ics.texera.model.jooq.generated.Indexes;
import edu.uci.ics.texera.model.jooq.generated.Keys;
import edu.uci.ics.texera.model.jooq.generated.TexeraDb;
import edu.uci.ics.texera.model.jooq.generated.enums.DatasetUserAccessPrivilege;
import edu.uci.ics.texera.model.jooq.generated.tables.records.DatasetUserAccessRecord;

import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row3;
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
public class DatasetUserAccess extends TableImpl<DatasetUserAccessRecord> {

    private static final long serialVersionUID = -345038433;

    /**
     * The reference instance of <code>texera_db.dataset_user_access</code>
     */
    public static final DatasetUserAccess DATASET_USER_ACCESS = new DatasetUserAccess();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<DatasetUserAccessRecord> getRecordType() {
        return DatasetUserAccessRecord.class;
    }

    /**
     * The column <code>texera_db.dataset_user_access.did</code>.
     */
    public final TableField<DatasetUserAccessRecord, UInteger> DID = createField(DSL.name("did"), org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>texera_db.dataset_user_access.uid</code>.
     */
    public final TableField<DatasetUserAccessRecord, UInteger> UID = createField(DSL.name("uid"), org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>texera_db.dataset_user_access.privilege</code>.
     */
    public final TableField<DatasetUserAccessRecord, DatasetUserAccessPrivilege> PRIVILEGE = createField(DSL.name("privilege"), org.jooq.impl.SQLDataType.VARCHAR(5).nullable(false).defaultValue(org.jooq.impl.DSL.inline("NONE", org.jooq.impl.SQLDataType.VARCHAR)).asEnumDataType(edu.uci.ics.texera.model.jooq.generated.enums.DatasetUserAccessPrivilege.class), this, "");

    /**
     * Create a <code>texera_db.dataset_user_access</code> table reference
     */
    public DatasetUserAccess() {
        this(DSL.name("dataset_user_access"), null);
    }

    /**
     * Create an aliased <code>texera_db.dataset_user_access</code> table reference
     */
    public DatasetUserAccess(String alias) {
        this(DSL.name(alias), DATASET_USER_ACCESS);
    }

    /**
     * Create an aliased <code>texera_db.dataset_user_access</code> table reference
     */
    public DatasetUserAccess(Name alias) {
        this(alias, DATASET_USER_ACCESS);
    }

    private DatasetUserAccess(Name alias, Table<DatasetUserAccessRecord> aliased) {
        this(alias, aliased, null);
    }

    private DatasetUserAccess(Name alias, Table<DatasetUserAccessRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> DatasetUserAccess(Table<O> child, ForeignKey<O, DatasetUserAccessRecord> key) {
        super(child, key, DATASET_USER_ACCESS);
    }

    @Override
    public Schema getSchema() {
        return TexeraDb.TEXERA_DB;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.DATASET_USER_ACCESS_PRIMARY, Indexes.DATASET_USER_ACCESS_UID);
    }

    @Override
    public UniqueKey<DatasetUserAccessRecord> getPrimaryKey() {
        return Keys.KEY_DATASET_USER_ACCESS_PRIMARY;
    }

    @Override
    public List<UniqueKey<DatasetUserAccessRecord>> getKeys() {
        return Arrays.<UniqueKey<DatasetUserAccessRecord>>asList(Keys.KEY_DATASET_USER_ACCESS_PRIMARY);
    }

    @Override
    public List<ForeignKey<DatasetUserAccessRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<DatasetUserAccessRecord, ?>>asList(Keys.DATASET_USER_ACCESS_IBFK_1, Keys.DATASET_USER_ACCESS_IBFK_2);
    }

    public Dataset dataset() {
        return new Dataset(this, Keys.DATASET_USER_ACCESS_IBFK_1);
    }

    public User user() {
        return new User(this, Keys.DATASET_USER_ACCESS_IBFK_2);
    }

    @Override
    public DatasetUserAccess as(String alias) {
        return new DatasetUserAccess(DSL.name(alias), this);
    }

    @Override
    public DatasetUserAccess as(Name alias) {
        return new DatasetUserAccess(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public DatasetUserAccess rename(String name) {
        return new DatasetUserAccess(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public DatasetUserAccess rename(Name name) {
        return new DatasetUserAccess(name, null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<UInteger, UInteger, DatasetUserAccessPrivilege> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}
