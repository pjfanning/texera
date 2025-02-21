/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.dao.jooq.generated.tables.interfaces;


import java.io.Serializable;
import java.sql.Timestamp;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IDataset extends Serializable {

    /**
     * Setter for <code>texera_db.dataset.did</code>.
     */
    public void setDid(Integer value);

    /**
     * Getter for <code>texera_db.dataset.did</code>.
     */
    public Integer getDid();

    /**
     * Setter for <code>texera_db.dataset.owner_uid</code>.
     */
    public void setOwnerUid(Integer value);

    /**
     * Getter for <code>texera_db.dataset.owner_uid</code>.
     */
    public Integer getOwnerUid();

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
    public void setIsPublic(Boolean value);

    /**
     * Getter for <code>texera_db.dataset.is_public</code>.
     */
    public Boolean getIsPublic();

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
     * Load data from another generated Record/POJO implementing the common
     * interface IDataset
     */
    public void from(IDataset from);

    /**
     * Copy data into another generated Record/POJO implementing the common
     * interface IDataset
     */
    public <E extends IDataset> E into(E into);
}
