/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.web.model.jooq.generated.tables.daos;


import edu.uci.ics.texera.web.model.jooq.generated.tables.File;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.FileRecord;

import java.sql.Timestamp;
import java.util.List;

import org.jooq.Configuration;
import org.jooq.impl.DAOImpl;
import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class FileDao extends DAOImpl<FileRecord, edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File, UInteger> {

    /**
     * Create a new FileDao without any configuration
     */
    public FileDao() {
        super(File.FILE, edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File.class);
    }

    /**
     * Create a new FileDao with an attached configuration
     */
    public FileDao(Configuration configuration) {
        super(File.FILE, edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File.class, configuration);
    }

    @Override
    public UInteger getId(edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File object) {
        return object.getFid();
    }

    /**
     * Fetch records that have <code>uid BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File> fetchRangeOfUid(UInteger lowerInclusive, UInteger upperInclusive) {
        return fetchRange(File.FILE.UID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>uid IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File> fetchByUid(UInteger... values) {
        return fetch(File.FILE.UID, values);
    }

    /**
     * Fetch records that have <code>fid BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File> fetchRangeOfFid(UInteger lowerInclusive, UInteger upperInclusive) {
        return fetchRange(File.FILE.FID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>fid IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File> fetchByFid(UInteger... values) {
        return fetch(File.FILE.FID, values);
    }

    /**
     * Fetch a unique record that has <code>fid = value</code>
     */
    public edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File fetchOneByFid(UInteger value) {
        return fetchOne(File.FILE.FID, value);
    }

    /**
     * Fetch records that have <code>size BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File> fetchRangeOfSize(UInteger lowerInclusive, UInteger upperInclusive) {
        return fetchRange(File.FILE.SIZE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>size IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File> fetchBySize(UInteger... values) {
        return fetch(File.FILE.SIZE, values);
    }

    /**
     * Fetch records that have <code>name BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File> fetchRangeOfName(String lowerInclusive, String upperInclusive) {
        return fetchRange(File.FILE.NAME, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>name IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File> fetchByName(String... values) {
        return fetch(File.FILE.NAME, values);
    }

    /**
     * Fetch records that have <code>path BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File> fetchRangeOfPath(String lowerInclusive, String upperInclusive) {
        return fetchRange(File.FILE.PATH, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>path IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File> fetchByPath(String... values) {
        return fetch(File.FILE.PATH, values);
    }

    /**
     * Fetch records that have <code>description BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File> fetchRangeOfDescription(String lowerInclusive, String upperInclusive) {
        return fetchRange(File.FILE.DESCRIPTION, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>description IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File> fetchByDescription(String... values) {
        return fetch(File.FILE.DESCRIPTION, values);
    }

    /**
     * Fetch records that have <code>upload_time BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File> fetchRangeOfUploadTime(Timestamp lowerInclusive, Timestamp upperInclusive) {
        return fetchRange(File.FILE.UPLOAD_TIME, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>upload_time IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.File> fetchByUploadTime(Timestamp... values) {
        return fetch(File.FILE.UPLOAD_TIME, values);
    }
}
