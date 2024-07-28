/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.web.model.jooq.generated.tables.daos;


import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowUserClones;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.WorkflowUserClonesRecord;

import java.util.List;

import org.jooq.Configuration;
import org.jooq.Record2;
import org.jooq.impl.DAOImpl;
import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class WorkflowUserClonesDao extends DAOImpl<WorkflowUserClonesRecord, edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.WorkflowUserClones, Record2<UInteger, UInteger>> {

    /**
     * Create a new WorkflowUserClonesDao without any configuration
     */
    public WorkflowUserClonesDao() {
        super(WorkflowUserClones.WORKFLOW_USER_CLONES, edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.WorkflowUserClones.class);
    }

    /**
     * Create a new WorkflowUserClonesDao with an attached configuration
     */
    public WorkflowUserClonesDao(Configuration configuration) {
        super(WorkflowUserClones.WORKFLOW_USER_CLONES, edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.WorkflowUserClones.class, configuration);
    }

    @Override
    public Record2<UInteger, UInteger> getId(edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.WorkflowUserClones object) {
        return compositeKeyRecord(object.getUid(), object.getWid());
    }

    /**
     * Fetch records that have <code>uid BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.WorkflowUserClones> fetchRangeOfUid(UInteger lowerInclusive, UInteger upperInclusive) {
        return fetchRange(WorkflowUserClones.WORKFLOW_USER_CLONES.UID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>uid IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.WorkflowUserClones> fetchByUid(UInteger... values) {
        return fetch(WorkflowUserClones.WORKFLOW_USER_CLONES.UID, values);
    }

    /**
     * Fetch records that have <code>wid BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.WorkflowUserClones> fetchRangeOfWid(UInteger lowerInclusive, UInteger upperInclusive) {
        return fetchRange(WorkflowUserClones.WORKFLOW_USER_CLONES.WID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>wid IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.WorkflowUserClones> fetchByWid(UInteger... values) {
        return fetch(WorkflowUserClones.WORKFLOW_USER_CLONES.WID, values);
    }
}
