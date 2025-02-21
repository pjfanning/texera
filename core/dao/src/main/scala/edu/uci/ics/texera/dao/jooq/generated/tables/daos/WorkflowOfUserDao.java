/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.dao.jooq.generated.tables.daos;


import edu.uci.ics.texera.dao.jooq.generated.tables.WorkflowOfUser;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.WorkflowOfUserRecord;

import java.util.List;

import org.jooq.Configuration;
import org.jooq.Record2;
import org.jooq.impl.DAOImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class WorkflowOfUserDao extends DAOImpl<WorkflowOfUserRecord, edu.uci.ics.texera.dao.jooq.generated.tables.pojos.WorkflowOfUser, Record2<Integer, Integer>> {

    /**
     * Create a new WorkflowOfUserDao without any configuration
     */
    public WorkflowOfUserDao() {
        super(WorkflowOfUser.WORKFLOW_OF_USER, edu.uci.ics.texera.dao.jooq.generated.tables.pojos.WorkflowOfUser.class);
    }

    /**
     * Create a new WorkflowOfUserDao with an attached configuration
     */
    public WorkflowOfUserDao(Configuration configuration) {
        super(WorkflowOfUser.WORKFLOW_OF_USER, edu.uci.ics.texera.dao.jooq.generated.tables.pojos.WorkflowOfUser.class, configuration);
    }

    @Override
    public Record2<Integer, Integer> getId(edu.uci.ics.texera.dao.jooq.generated.tables.pojos.WorkflowOfUser object) {
        return compositeKeyRecord(object.getUid(), object.getWid());
    }

    /**
     * Fetch records that have <code>uid BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<edu.uci.ics.texera.dao.jooq.generated.tables.pojos.WorkflowOfUser> fetchRangeOfUid(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(WorkflowOfUser.WORKFLOW_OF_USER.UID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>uid IN (values)</code>
     */
    public List<edu.uci.ics.texera.dao.jooq.generated.tables.pojos.WorkflowOfUser> fetchByUid(Integer... values) {
        return fetch(WorkflowOfUser.WORKFLOW_OF_USER.UID, values);
    }

    /**
     * Fetch records that have <code>wid BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<edu.uci.ics.texera.dao.jooq.generated.tables.pojos.WorkflowOfUser> fetchRangeOfWid(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(WorkflowOfUser.WORKFLOW_OF_USER.WID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>wid IN (values)</code>
     */
    public List<edu.uci.ics.texera.dao.jooq.generated.tables.pojos.WorkflowOfUser> fetchByWid(Integer... values) {
        return fetch(WorkflowOfUser.WORKFLOW_OF_USER.WID, values);
    }
}
