/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.web.model.jooq.generated.tables.daos;


import edu.uci.ics.texera.web.model.jooq.generated.enums.UserRole;
import edu.uci.ics.texera.web.model.jooq.generated.tables.User;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.UserRecord;

import java.util.List;

import org.jooq.Configuration;
import org.jooq.impl.DAOImpl;
import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UserDao extends DAOImpl<UserRecord, edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User, UInteger> {

    /**
     * Create a new UserDao without any configuration
     */
    public UserDao() {
        super(User.USER, edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User.class);
    }

    /**
     * Create a new UserDao with an attached configuration
     */
    public UserDao(Configuration configuration) {
        super(User.USER, edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User.class, configuration);
    }

    @Override
    public UInteger getId(edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User object) {
        return object.getUid();
    }

    /**
     * Fetch records that have <code>uid BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User> fetchRangeOfUid(UInteger lowerInclusive, UInteger upperInclusive) {
        return fetchRange(User.USER.UID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>uid IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User> fetchByUid(UInteger... values) {
        return fetch(User.USER.UID, values);
    }

    /**
     * Fetch a unique record that has <code>uid = value</code>
     */
    public edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User fetchOneByUid(UInteger value) {
        return fetchOne(User.USER.UID, value);
    }

    /**
     * Fetch records that have <code>name BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User> fetchRangeOfName(String lowerInclusive, String upperInclusive) {
        return fetchRange(User.USER.NAME, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>name IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User> fetchByName(String... values) {
        return fetch(User.USER.NAME, values);
    }

    /**
     * Fetch records that have <code>email BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User> fetchRangeOfEmail(String lowerInclusive, String upperInclusive) {
        return fetchRange(User.USER.EMAIL, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>email IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User> fetchByEmail(String... values) {
        return fetch(User.USER.EMAIL, values);
    }

    /**
     * Fetch a unique record that has <code>email = value</code>
     */
    public edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User fetchOneByEmail(String value) {
        return fetchOne(User.USER.EMAIL, value);
    }

    /**
     * Fetch records that have <code>password BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User> fetchRangeOfPassword(String lowerInclusive, String upperInclusive) {
        return fetchRange(User.USER.PASSWORD, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>password IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User> fetchByPassword(String... values) {
        return fetch(User.USER.PASSWORD, values);
    }

    /**
     * Fetch records that have <code>google_id BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User> fetchRangeOfGoogleId(String lowerInclusive, String upperInclusive) {
        return fetchRange(User.USER.GOOGLE_ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>google_id IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User> fetchByGoogleId(String... values) {
        return fetch(User.USER.GOOGLE_ID, values);
    }

    /**
     * Fetch a unique record that has <code>google_id = value</code>
     */
    public edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User fetchOneByGoogleId(String value) {
        return fetchOne(User.USER.GOOGLE_ID, value);
    }

    /**
     * Fetch records that have <code>role BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User> fetchRangeOfRole(UserRole lowerInclusive, UserRole upperInclusive) {
        return fetchRange(User.USER.ROLE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>role IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User> fetchByRole(UserRole... values) {
        return fetch(User.USER.ROLE, values);
    }

    /**
     * Fetch records that have <code>google_avatar BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User> fetchRangeOfGoogleAvatar(String lowerInclusive, String upperInclusive) {
        return fetchRange(User.USER.GOOGLE_AVATAR, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>google_avatar IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User> fetchByGoogleAvatar(String... values) {
        return fetch(User.USER.GOOGLE_AVATAR, values);
    }
}
