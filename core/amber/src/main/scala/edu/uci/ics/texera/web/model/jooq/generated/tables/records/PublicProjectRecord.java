/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.web.model.jooq.generated.tables.records;


import edu.uci.ics.texera.web.model.jooq.generated.tables.PublicProject;
import edu.uci.ics.texera.web.model.jooq.generated.tables.interfaces.IPublicProject;

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
public class PublicProjectRecord extends UpdatableRecordImpl<PublicProjectRecord> implements Record2<UInteger, UInteger>, IPublicProject {

    private static final long serialVersionUID = 89759719;

    /**
     * Setter for <code>texera_db.public_project.pid</code>.
     */
    @Override
    public void setPid(UInteger value) {
        set(0, value);
    }

    /**
     * Getter for <code>texera_db.public_project.pid</code>.
     */
    @Override
    public UInteger getPid() {
        return (UInteger) get(0);
    }

    /**
     * Setter for <code>texera_db.public_project.uid</code>.
     */
    @Override
    public void setUid(UInteger value) {
        set(1, value);
    }

    /**
     * Getter for <code>texera_db.public_project.uid</code>.
     */
    @Override
    public UInteger getUid() {
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
        return PublicProject.PUBLIC_PROJECT.PID;
    }

    @Override
    public Field<UInteger> field2() {
        return PublicProject.PUBLIC_PROJECT.UID;
    }

    @Override
    public UInteger component1() {
        return getPid();
    }

    @Override
    public UInteger component2() {
        return getUid();
    }

    @Override
    public UInteger value1() {
        return getPid();
    }

    @Override
    public UInteger value2() {
        return getUid();
    }

    @Override
    public PublicProjectRecord value1(UInteger value) {
        setPid(value);
        return this;
    }

    @Override
    public PublicProjectRecord value2(UInteger value) {
        setUid(value);
        return this;
    }

    @Override
    public PublicProjectRecord values(UInteger value1, UInteger value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IPublicProject from) {
        setPid(from.getPid());
        setUid(from.getUid());
    }

    @Override
    public <E extends IPublicProject> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PublicProjectRecord
     */
    public PublicProjectRecord() {
        super(PublicProject.PUBLIC_PROJECT);
    }

    /**
     * Create a detached, initialised PublicProjectRecord
     */
    public PublicProjectRecord(UInteger pid, UInteger uid) {
        super(PublicProject.PUBLIC_PROJECT);

        set(0, pid);
        set(1, uid);
    }
}
