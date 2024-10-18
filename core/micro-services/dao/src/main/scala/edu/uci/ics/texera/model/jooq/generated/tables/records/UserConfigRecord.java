/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.model.jooq.generated.tables.records;


import edu.uci.ics.texera.model.jooq.generated.tables.UserConfig;
import edu.uci.ics.texera.model.jooq.generated.tables.interfaces.IUserConfig;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UserConfigRecord extends UpdatableRecordImpl<UserConfigRecord> implements Record3<UInteger, String, String>, IUserConfig {

    private static final long serialVersionUID = -765295317;

    /**
     * Setter for <code>texera_db.user_config.uid</code>.
     */
    @Override
    public void setUid(UInteger value) {
        set(0, value);
    }

    /**
     * Getter for <code>texera_db.user_config.uid</code>.
     */
    @Override
    public UInteger getUid() {
        return (UInteger) get(0);
    }

    /**
     * Setter for <code>texera_db.user_config.key</code>.
     */
    @Override
    public void setKey(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>texera_db.user_config.key</code>.
     */
    @Override
    public String getKey() {
        return (String) get(1);
    }

    /**
     * Setter for <code>texera_db.user_config.value</code>.
     */
    @Override
    public void setValue(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>texera_db.user_config.value</code>.
     */
    @Override
    public String getValue() {
        return (String) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<UInteger, String> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<UInteger, String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<UInteger, String, String> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return UserConfig.USER_CONFIG.UID;
    }

    @Override
    public Field<String> field2() {
        return UserConfig.USER_CONFIG.KEY;
    }

    @Override
    public Field<String> field3() {
        return UserConfig.USER_CONFIG.VALUE;
    }

    @Override
    public UInteger component1() {
        return getUid();
    }

    @Override
    public String component2() {
        return getKey();
    }

    @Override
    public String component3() {
        return getValue();
    }

    @Override
    public UInteger value1() {
        return getUid();
    }

    @Override
    public String value2() {
        return getKey();
    }

    @Override
    public String value3() {
        return getValue();
    }

    @Override
    public UserConfigRecord value1(UInteger value) {
        setUid(value);
        return this;
    }

    @Override
    public UserConfigRecord value2(String value) {
        setKey(value);
        return this;
    }

    @Override
    public UserConfigRecord value3(String value) {
        setValue(value);
        return this;
    }

    @Override
    public UserConfigRecord values(UInteger value1, String value2, String value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IUserConfig from) {
        setUid(from.getUid());
        setKey(from.getKey());
        setValue(from.getValue());
    }

    @Override
    public <E extends IUserConfig> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached UserConfigRecord
     */
    public UserConfigRecord() {
        super(UserConfig.USER_CONFIG);
    }

    /**
     * Create a detached, initialised UserConfigRecord
     */
    public UserConfigRecord(UInteger uid, String key, String value) {
        super(UserConfig.USER_CONFIG);

        set(0, uid);
        set(1, key);
        set(2, value);
    }
}
