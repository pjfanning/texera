/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.model.jooq.generated.tables.interfaces;


import edu.uci.ics.texera.model.jooq.generated.enums.UserRole;

import java.io.Serializable;

import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IUser extends Serializable {

    /**
     * Setter for <code>texera_db.user.uid</code>.
     */
    public void setUid(UInteger value);

    /**
     * Getter for <code>texera_db.user.uid</code>.
     */
    public UInteger getUid();

    /**
     * Setter for <code>texera_db.user.name</code>.
     */
    public void setName(String value);

    /**
     * Getter for <code>texera_db.user.name</code>.
     */
    public String getName();

    /**
     * Setter for <code>texera_db.user.email</code>.
     */
    public void setEmail(String value);

    /**
     * Getter for <code>texera_db.user.email</code>.
     */
    public String getEmail();

    /**
     * Setter for <code>texera_db.user.password</code>.
     */
    public void setPassword(String value);

    /**
     * Getter for <code>texera_db.user.password</code>.
     */
    public String getPassword();

    /**
     * Setter for <code>texera_db.user.google_id</code>.
     */
    public void setGoogleId(String value);

    /**
     * Getter for <code>texera_db.user.google_id</code>.
     */
    public String getGoogleId();

    /**
     * Setter for <code>texera_db.user.role</code>.
     */
    public void setRole(UserRole value);

    /**
     * Getter for <code>texera_db.user.role</code>.
     */
    public UserRole getRole();

    /**
     * Setter for <code>texera_db.user.google_avatar</code>.
     */
    public void setGoogleAvatar(String value);

    /**
     * Getter for <code>texera_db.user.google_avatar</code>.
     */
    public String getGoogleAvatar();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface IUser
     */
    public void from(edu.uci.ics.texera.model.jooq.generated.tables.interfaces.IUser from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface IUser
     */
    public <E extends edu.uci.ics.texera.model.jooq.generated.tables.interfaces.IUser> E into(E into);
}
