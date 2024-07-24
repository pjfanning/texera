/*
 * This file is generated by jOOQ.
 */
package web.model.jooq.generated.tables.daos;


import java.util.List;

import org.jooq.Configuration;
import org.jooq.Record2;
import org.jooq.impl.DAOImpl;
import org.jooq.types.UInteger;

import web.model.jooq.generated.enums.DatasetUserAccessPrivilege;
import web.model.jooq.generated.tables.DatasetUserAccess;
import web.model.jooq.generated.tables.records.DatasetUserAccessRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DatasetUserAccessDao extends DAOImpl<DatasetUserAccessRecord, web.model.jooq.generated.tables.pojos.DatasetUserAccess, Record2<UInteger, UInteger>> {

    /**
     * Create a new DatasetUserAccessDao without any configuration
     */
    public DatasetUserAccessDao() {
        super(DatasetUserAccess.DATASET_USER_ACCESS, web.model.jooq.generated.tables.pojos.DatasetUserAccess.class);
    }

    /**
     * Create a new DatasetUserAccessDao with an attached configuration
     */
    public DatasetUserAccessDao(Configuration configuration) {
        super(DatasetUserAccess.DATASET_USER_ACCESS, web.model.jooq.generated.tables.pojos.DatasetUserAccess.class, configuration);
    }

    @Override
    public Record2<UInteger, UInteger> getId(web.model.jooq.generated.tables.pojos.DatasetUserAccess object) {
        return compositeKeyRecord(object.getDid(), object.getUid());
    }

    /**
     * Fetch records that have <code>did BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<web.model.jooq.generated.tables.pojos.DatasetUserAccess> fetchRangeOfDid(UInteger lowerInclusive, UInteger upperInclusive) {
        return fetchRange(DatasetUserAccess.DATASET_USER_ACCESS.DID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>did IN (values)</code>
     */
    public List<web.model.jooq.generated.tables.pojos.DatasetUserAccess> fetchByDid(UInteger... values) {
        return fetch(DatasetUserAccess.DATASET_USER_ACCESS.DID, values);
    }

    /**
     * Fetch records that have <code>uid BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<web.model.jooq.generated.tables.pojos.DatasetUserAccess> fetchRangeOfUid(UInteger lowerInclusive, UInteger upperInclusive) {
        return fetchRange(DatasetUserAccess.DATASET_USER_ACCESS.UID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>uid IN (values)</code>
     */
    public List<web.model.jooq.generated.tables.pojos.DatasetUserAccess> fetchByUid(UInteger... values) {
        return fetch(DatasetUserAccess.DATASET_USER_ACCESS.UID, values);
    }

    /**
     * Fetch records that have <code>privilege BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<web.model.jooq.generated.tables.pojos.DatasetUserAccess> fetchRangeOfPrivilege(DatasetUserAccessPrivilege lowerInclusive, DatasetUserAccessPrivilege upperInclusive) {
        return fetchRange(DatasetUserAccess.DATASET_USER_ACCESS.PRIVILEGE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>privilege IN (values)</code>
     */
    public List<web.model.jooq.generated.tables.pojos.DatasetUserAccess> fetchByPrivilege(DatasetUserAccessPrivilege... values) {
        return fetch(DatasetUserAccess.DATASET_USER_ACCESS.PRIVILEGE, values);
    }
}
