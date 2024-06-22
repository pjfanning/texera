/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.web.model.jooq.generated.tables.daos;


import edu.uci.ics.texera.web.model.jooq.generated.tables.Dataset;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.DatasetRecord;

import java.sql.Timestamp;
import java.util.List;

import org.jooq.Configuration;
import org.jooq.impl.DAOImpl;
import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DatasetDao extends DAOImpl<DatasetRecord, edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset, UInteger> {

    /**
     * Create a new DatasetDao without any configuration
     */
    public DatasetDao() {
        super(Dataset.DATASET, edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset.class);
    }

    /**
     * Create a new DatasetDao with an attached configuration
     */
    public DatasetDao(Configuration configuration) {
        super(Dataset.DATASET, edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset.class, configuration);
    }

    @Override
    public UInteger getId(edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset object) {
        return object.getDid();
    }

    /**
     * Fetch records that have <code>did BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset> fetchRangeOfDid(UInteger lowerInclusive, UInteger upperInclusive) {
        return fetchRange(Dataset.DATASET.DID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>did IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset> fetchByDid(UInteger... values) {
        return fetch(Dataset.DATASET.DID, values);
    }

    /**
     * Fetch a unique record that has <code>did = value</code>
     */
    public edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset fetchOneByDid(UInteger value) {
        return fetchOne(Dataset.DATASET.DID, value);
    }

    /**
     * Fetch records that have <code>owner_uid BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset> fetchRangeOfOwnerUid(UInteger lowerInclusive, UInteger upperInclusive) {
        return fetchRange(Dataset.DATASET.OWNER_UID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>owner_uid IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset> fetchByOwnerUid(UInteger... values) {
        return fetch(Dataset.DATASET.OWNER_UID, values);
    }

    /**
     * Fetch records that have <code>name BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset> fetchRangeOfName(String lowerInclusive, String upperInclusive) {
        return fetchRange(Dataset.DATASET.NAME, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>name IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset> fetchByName(String... values) {
        return fetch(Dataset.DATASET.NAME, values);
    }

    /**
     * Fetch records that have <code>is_public BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset> fetchRangeOfIsPublic(Byte lowerInclusive, Byte upperInclusive) {
        return fetchRange(Dataset.DATASET.IS_PUBLIC, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>is_public IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset> fetchByIsPublic(Byte... values) {
        return fetch(Dataset.DATASET.IS_PUBLIC, values);
    }

    /**
     * Fetch records that have <code>description BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset> fetchRangeOfDescription(String lowerInclusive, String upperInclusive) {
        return fetchRange(Dataset.DATASET.DESCRIPTION, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>description IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset> fetchByDescription(String... values) {
        return fetch(Dataset.DATASET.DESCRIPTION, values);
    }

    /**
     * Fetch records that have <code>creation_time BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset> fetchRangeOfCreationTime(Timestamp lowerInclusive, Timestamp upperInclusive) {
        return fetchRange(Dataset.DATASET.CREATION_TIME, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>creation_time IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset> fetchByCreationTime(Timestamp... values) {
        return fetch(Dataset.DATASET.CREATION_TIME, values);
    }
}
