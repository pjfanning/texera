/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.dao.jooq.generated.tables.daos;


import edu.uci.ics.texera.dao.jooq.generated.tables.DatasetUserLikes;
import edu.uci.ics.texera.dao.jooq.generated.tables.records.DatasetUserLikesRecord;

import java.util.List;

import org.jooq.Configuration;
import org.jooq.Record2;
import org.jooq.impl.DAOImpl;
import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DatasetUserLikesDao extends DAOImpl<DatasetUserLikesRecord, edu.uci.ics.texera.dao.jooq.generated.tables.pojos.DatasetUserLikes, Record2<UInteger, UInteger>> {

    /**
     * Create a new DatasetUserLikesDao without any configuration
     */
    public DatasetUserLikesDao() {
        super(DatasetUserLikes.DATASET_USER_LIKES, edu.uci.ics.texera.dao.jooq.generated.tables.pojos.DatasetUserLikes.class);
    }

    /**
     * Create a new DatasetUserLikesDao with an attached configuration
     */
    public DatasetUserLikesDao(Configuration configuration) {
        super(DatasetUserLikes.DATASET_USER_LIKES, edu.uci.ics.texera.dao.jooq.generated.tables.pojos.DatasetUserLikes.class, configuration);
    }

    @Override
    public Record2<UInteger, UInteger> getId(edu.uci.ics.texera.dao.jooq.generated.tables.pojos.DatasetUserLikes object) {
        return compositeKeyRecord(object.getUid(), object.getDid());
    }

    /**
     * Fetch records that have <code>uid BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.dao.jooq.generated.tables.pojos.DatasetUserLikes> fetchRangeOfUid(UInteger lowerInclusive, UInteger upperInclusive) {
        return fetchRange(DatasetUserLikes.DATASET_USER_LIKES.UID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>uid IN (values)</code>
     */
    public List<edu.uci.ics.texera.dao.jooq.generated.tables.pojos.DatasetUserLikes> fetchByUid(UInteger... values) {
        return fetch(DatasetUserLikes.DATASET_USER_LIKES.UID, values);
    }

    /**
     * Fetch records that have <code>did BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.dao.jooq.generated.tables.pojos.DatasetUserLikes> fetchRangeOfDid(UInteger lowerInclusive, UInteger upperInclusive) {
        return fetchRange(DatasetUserLikes.DATASET_USER_LIKES.DID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>did IN (values)</code>
     */
    public List<edu.uci.ics.texera.dao.jooq.generated.tables.pojos.DatasetUserLikes> fetchByDid(UInteger... values) {
        return fetch(DatasetUserLikes.DATASET_USER_LIKES.DID, values);
    }
}
