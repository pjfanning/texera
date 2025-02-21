/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.dao.jooq.generated.tables.records;


import edu.uci.ics.texera.dao.jooq.generated.tables.UserActivity;
import edu.uci.ics.texera.dao.jooq.generated.tables.interfaces.IUserActivity;

import java.sql.Timestamp;

import org.jooq.Field;
import org.jooq.Record6;
import org.jooq.Row6;
import org.jooq.impl.TableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UserActivityRecord extends TableRecordImpl<UserActivityRecord> implements Record6<Integer, Integer, String, String, String, Timestamp>, IUserActivity {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>texera_db.user_activity.uid</code>.
     */
    @Override
    public void setUid(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>texera_db.user_activity.uid</code>.
     */
    @Override
    public Integer getUid() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>texera_db.user_activity.id</code>.
     */
    @Override
    public void setId(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>texera_db.user_activity.id</code>.
     */
    @Override
    public Integer getId() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>texera_db.user_activity.type</code>.
     */
    @Override
    public void setType(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>texera_db.user_activity.type</code>.
     */
    @Override
    public String getType() {
        return (String) get(2);
    }

    /**
     * Setter for <code>texera_db.user_activity.ip</code>.
     */
    @Override
    public void setIp(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>texera_db.user_activity.ip</code>.
     */
    @Override
    public String getIp() {
        return (String) get(3);
    }

    /**
     * Setter for <code>texera_db.user_activity.activate</code>.
     */
    @Override
    public void setActivate(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>texera_db.user_activity.activate</code>.
     */
    @Override
    public String getActivate() {
        return (String) get(4);
    }

    /**
     * Setter for <code>texera_db.user_activity.activity_time</code>.
     */
    @Override
    public void setActivityTime(Timestamp value) {
        set(5, value);
    }

    /**
     * Getter for <code>texera_db.user_activity.activity_time</code>.
     */
    @Override
    public Timestamp getActivityTime() {
        return (Timestamp) get(5);
    }

    // -------------------------------------------------------------------------
    // Record6 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row6<Integer, Integer, String, String, String, Timestamp> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    @Override
    public Row6<Integer, Integer, String, String, String, Timestamp> valuesRow() {
        return (Row6) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return UserActivity.USER_ACTIVITY.UID;
    }

    @Override
    public Field<Integer> field2() {
        return UserActivity.USER_ACTIVITY.ID;
    }

    @Override
    public Field<String> field3() {
        return UserActivity.USER_ACTIVITY.TYPE;
    }

    @Override
    public Field<String> field4() {
        return UserActivity.USER_ACTIVITY.IP;
    }

    @Override
    public Field<String> field5() {
        return UserActivity.USER_ACTIVITY.ACTIVATE;
    }

    @Override
    public Field<Timestamp> field6() {
        return UserActivity.USER_ACTIVITY.ACTIVITY_TIME;
    }

    @Override
    public Integer component1() {
        return getUid();
    }

    @Override
    public Integer component2() {
        return getId();
    }

    @Override
    public String component3() {
        return getType();
    }

    @Override
    public String component4() {
        return getIp();
    }

    @Override
    public String component5() {
        return getActivate();
    }

    @Override
    public Timestamp component6() {
        return getActivityTime();
    }

    @Override
    public Integer value1() {
        return getUid();
    }

    @Override
    public Integer value2() {
        return getId();
    }

    @Override
    public String value3() {
        return getType();
    }

    @Override
    public String value4() {
        return getIp();
    }

    @Override
    public String value5() {
        return getActivate();
    }

    @Override
    public Timestamp value6() {
        return getActivityTime();
    }

    @Override
    public UserActivityRecord value1(Integer value) {
        setUid(value);
        return this;
    }

    @Override
    public UserActivityRecord value2(Integer value) {
        setId(value);
        return this;
    }

    @Override
    public UserActivityRecord value3(String value) {
        setType(value);
        return this;
    }

    @Override
    public UserActivityRecord value4(String value) {
        setIp(value);
        return this;
    }

    @Override
    public UserActivityRecord value5(String value) {
        setActivate(value);
        return this;
    }

    @Override
    public UserActivityRecord value6(Timestamp value) {
        setActivityTime(value);
        return this;
    }

    @Override
    public UserActivityRecord values(Integer value1, Integer value2, String value3, String value4, String value5, Timestamp value6) {
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
    public void from(IUserActivity from) {
        setUid(from.getUid());
        setId(from.getId());
        setType(from.getType());
        setIp(from.getIp());
        setActivate(from.getActivate());
        setActivityTime(from.getActivityTime());
    }

    @Override
    public <E extends IUserActivity> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached UserActivityRecord
     */
    public UserActivityRecord() {
        super(UserActivity.USER_ACTIVITY);
    }

    /**
     * Create a detached, initialised UserActivityRecord
     */
    public UserActivityRecord(Integer uid, Integer id, String type, String ip, String activate, Timestamp activityTime) {
        super(UserActivity.USER_ACTIVITY);

        setUid(uid);
        setId(id);
        setType(type);
        setIp(ip);
        setActivate(activate);
        setActivityTime(activityTime);
    }

    /**
     * Create a detached, initialised UserActivityRecord
     */
    public UserActivityRecord(edu.uci.ics.texera.dao.jooq.generated.tables.pojos.UserActivity value) {
        super(UserActivity.USER_ACTIVITY);

        if (value != null) {
            setUid(value.getUid());
            setId(value.getId());
            setType(value.getType());
            setIp(value.getIp());
            setActivate(value.getActivate());
            setActivityTime(value.getActivityTime());
        }
    }
}
