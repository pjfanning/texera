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
public class PodRecord extends UpdatableRecordImpl<PodRecord> implements Record6<UInteger, String, String, UInteger, Timestamp, Timestamp>, IPod {

    private static final long serialVersionUID = -109891816;

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
     * Setter for <code>texera_db.pod.name</code>.
     */
    @Override
    public void setName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>texera_db.pod.name</code>.
     */
    @Override
    public String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>texera_db.pod.pod_uid</code>.
     */
    @Override
    public void setPodUid(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>texera_db.pod.pod_uid</code>.
     */
    @Override
    public String getPodUid() {
        return (String) get(2);
    }

    /**
     * Setter for <code>texera_db.pod.pod_id</code>.
     */
    @Override
    public void setPodId(UInteger value) {
        set(3, value);
    }

    /**
     * Getter for <code>texera_db.pod.pod_id</code>.
     */
    @Override
    public UInteger getPodId() {
        return (UInteger) get(3);
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
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record6 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row6<UInteger, String, String, UInteger, Timestamp, Timestamp> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    @Override
    public Row6<UInteger, String, String, UInteger, Timestamp, Timestamp> valuesRow() {
        return (Row6) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return Pod.POD.UID;
    }

    @Override
    public Field<String> field2() {
        return Pod.POD.NAME;
    }

    @Override
    public Field<String> field3() {
        return Pod.POD.POD_UID;
    }

    @Override
    public Field<UInteger> field4() {
        return Pod.POD.POD_ID;
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
    public String component2() {
        return getName();
    }

    @Override
    public String component3() {
        return getPodUid();
    }

    @Override
    public UInteger component4() {
        return getPodId();
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
    public String value2() {
        return getName();
    }

    @Override
    public String value3() {
        return getPodUid();
    }

    @Override
    public UInteger value4() {
        return getPodId();
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
    public PodRecord value2(String value) {
        setName(value);
        return this;
    }

    @Override
    public PodRecord value3(String value) {
        setPodUid(value);
        return this;
    }

    @Override
    public PodRecord value4(UInteger value) {
        setPodId(value);
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
    public PodRecord values(UInteger value1, String value2, String value3, UInteger value4, Timestamp value5, Timestamp value6) {
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
        setName(from.getName());
        setPodUid(from.getPodUid());
        setPodId(from.getPodId());
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
    public PodRecord(UInteger uid, String name, String podUid, UInteger podId, Timestamp creationTime, Timestamp terminateTime) {
        super(Pod.POD);

        set(0, uid);
        set(1, name);
        set(2, podUid);
        set(3, podId);
        set(4, creationTime);
        set(5, terminateTime);
    }
}
