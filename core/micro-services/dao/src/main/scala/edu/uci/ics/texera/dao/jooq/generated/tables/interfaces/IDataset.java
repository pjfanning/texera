/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.dao.jooq.generated.tables.interfaces;


import java.io.Serializable;
import java.sql.Timestamp;

import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IDataset extends Serializable {

    /**
     * Setter for <code>texera_db.dataset.did</code>.
     */
    public void setDid(UInteger value);

    /**
     * Getter for <code>texera_db.dataset.did</code>.
     */
    public UInteger getDid();

    /**
     * Setter for <code>texera_db.dataset.owner_uid</code>.
     */
    public void setOwnerUid(UInteger value);

    /**
     * Getter for <code>texera_db.dataset.owner_uid</code>.
     */
    public UInteger getOwnerUid();

    /**
     * Setter for <code>texera_db.dataset.name</code>.
     */
    public void setName(String value);

    /**
     * Getter for <code>texera_db.dataset.name</code>.
     */
    public String getName();

    /**
     * Setter for <code>texera_db.dataset.is_public</code>.
     */
    public void setIsPublic(Byte value);

    /**
     * Getter for <code>texera_db.dataset.is_public</code>.
     */
    public Byte getIsPublic();

    /**
     * Setter for <code>texera_db.dataset.description</code>.
     */
    public void setDescription(String value);

    /**
     * Getter for <code>texera_db.dataset.description</code>.
     */
    public String getDescription();

    /**
     * Setter for <code>texera_db.dataset.creation_time</code>.
     */
    public void setCreationTime(Timestamp value);

    /**
     * Getter for <code>texera_db.dataset.creation_time</code>.
     */
    public Timestamp getCreationTime();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface IDataset
     */
    public void from(edu.uci.ics.texera.dao.jooq.generated.tables.interfaces.IDataset from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface IDataset
     */
    public <E extends edu.uci.ics.texera.dao.jooq.generated.tables.interfaces.IDataset> E into(E into);
}
