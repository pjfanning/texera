/*
 * This file is generated by jOOQ.
 */
package web.model.jooq.generated.tables.records;


import java.sql.Timestamp;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record6;
import org.jooq.Row6;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import web.model.jooq.generated.tables.Pod;
import web.model.jooq.generated.tables.interfaces.IPod;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PodRecord extends UpdatableRecordImpl<PodRecord> implements Record6<UInteger, UInteger, String, String, Timestamp, Timestamp>, IPod {

    private static final long serialVersionUID = 1716849861;

    /**
     * Setter for <code>texera_db.pod.uid</code>.
     */
    @Override
    public void setUid(UInteger value) {
        set(0, value);
    }

    /**
     * Getter for <code>texera_db.pod.uid</code>.
     */
    @Override
    public UInteger getUid() {
        return (UInteger) get(0);
    }

    /**
     * Setter for <code>texera_db.pod.wid</code>.
     */
    @Override
    public void setWid(UInteger value) {
        set(1, value);
    }

    /**
     * Getter for <code>texera_db.pod.wid</code>.
     */
    @Override
    public UInteger getWid() {
        return (UInteger) get(1);
    }

    /**
     * Setter for <code>texera_db.pod.name</code>.
     */
    @Override
    public void setName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>texera_db.pod.name</code>.
     */
    @Override
    public String getName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>texera_db.pod.pod_uid</code>.
     */
    @Override
    public void setPodUid(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>texera_db.pod.pod_uid</code>.
     */
    @Override
    public String getPodUid() {
        return (String) get(3);
    }

    /**
     * Setter for <code>texera_db.pod.creation_time</code>.
     */
    @Override
    public void setCreationTime(Timestamp value) {
        set(4, value);
    }

    /**
     * Getter for <code>texera_db.pod.creation_time</code>.
     */
    @Override
    public Timestamp getCreationTime() {
        return (Timestamp) get(4);
    }

    /**
     * Setter for <code>texera_db.pod.terminate_time</code>.
     */
    @Override
    public void setTerminateTime(Timestamp value) {
        set(5, value);
    }

    /**
     * Getter for <code>texera_db.pod.terminate_time</code>.
     */
    @Override
    public Timestamp getTerminateTime() {
        return (Timestamp) get(5);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record6 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row6<UInteger, UInteger, String, String, Timestamp, Timestamp> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    @Override
    public Row6<UInteger, UInteger, String, String, Timestamp, Timestamp> valuesRow() {
        return (Row6) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return Pod.POD.UID;
    }

    @Override
    public Field<UInteger> field2() {
        return Pod.POD.WID;
    }

    @Override
    public Field<String> field3() {
        return Pod.POD.NAME;
    }

    @Override
    public Field<String> field4() {
        return Pod.POD.POD_UID;
    }

    @Override
    public Field<Timestamp> field5() {
        return Pod.POD.CREATION_TIME;
    }

    @Override
    public Field<Timestamp> field6() {
        return Pod.POD.TERMINATE_TIME;
    }

    @Override
    public UInteger component1() {
        return getUid();
    }

    @Override
    public UInteger component2() {
        return getWid();
    }

    @Override
    public String component3() {
        return getName();
    }

    @Override
    public String component4() {
        return getPodUid();
    }

    @Override
    public Timestamp component5() {
        return getCreationTime();
    }

    @Override
    public Timestamp component6() {
        return getTerminateTime();
    }

    @Override
    public UInteger value1() {
        return getUid();
    }

    @Override
    public UInteger value2() {
        return getWid();
    }

    @Override
    public String value3() {
        return getName();
    }

    @Override
    public String value4() {
        return getPodUid();
    }

    @Override
    public Timestamp value5() {
        return getCreationTime();
    }

    @Override
    public Timestamp value6() {
        return getTerminateTime();
    }

    @Override
    public PodRecord value1(UInteger value) {
        setUid(value);
        return this;
    }

    @Override
    public PodRecord value2(UInteger value) {
        setWid(value);
        return this;
    }

    @Override
    public PodRecord value3(String value) {
        setName(value);
        return this;
    }

    @Override
    public PodRecord value4(String value) {
        setPodUid(value);
        return this;
    }

    @Override
    public PodRecord value5(Timestamp value) {
        setCreationTime(value);
        return this;
    }

    @Override
    public PodRecord value6(Timestamp value) {
        setTerminateTime(value);
        return this;
    }

    @Override
    public PodRecord values(UInteger value1, UInteger value2, String value3, String value4, Timestamp value5, Timestamp value6) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IPod from) {
        setUid(from.getUid());
        setWid(from.getWid());
        setName(from.getName());
        setPodUid(from.getPodUid());
        setCreationTime(from.getCreationTime());
        setTerminateTime(from.getTerminateTime());
    }

    @Override
    public <E extends IPod> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PodRecord
     */
    public PodRecord() {
        super(Pod.POD);
    }

    /**
     * Create a detached, initialised PodRecord
     */
    public PodRecord(UInteger uid, UInteger wid, String name, String podUid, Timestamp creationTime, Timestamp terminateTime) {
        super(Pod.POD);

        set(0, uid);
        set(1, wid);
        set(2, name);
        set(3, podUid);
        set(4, creationTime);
        set(5, terminateTime);
    }
}
