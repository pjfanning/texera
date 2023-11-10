/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.web.model.jooq.generated.tables.interfaces;


import java.io.Serializable;
import java.sql.Timestamp;

import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IWorkflow extends Serializable {

    /**
     * Setter for <code>texera_db.workflow.name</code>.
     */
    public void setName(String value);

    /**
     * Getter for <code>texera_db.workflow.name</code>.
     */
    public String getName();

    /**
     * Setter for <code>texera_db.workflow.description</code>.
     */
    public void setDescription(String value);

    /**
     * Getter for <code>texera_db.workflow.description</code>.
     */
    public String getDescription();

    /**
     * Setter for <code>texera_db.workflow.wid</code>.
     */
    public void setWid(UInteger value);

    /**
     * Getter for <code>texera_db.workflow.wid</code>.
     */
    public UInteger getWid();

    /**
     * Setter for <code>texera_db.workflow.content</code>.
     */
    public void setContent(String value);

    /**
     * Getter for <code>texera_db.workflow.content</code>.
     */
    public String getContent();

    /**
     * Setter for <code>texera_db.workflow.creation_time</code>.
     */
    public void setCreationTime(Timestamp value);

    /**
     * Getter for <code>texera_db.workflow.creation_time</code>.
     */
    public Timestamp getCreationTime();

    /**
     * Setter for <code>texera_db.workflow.last_modified_time</code>.
     */
    public void setLastModifiedTime(Timestamp value);

    /**
     * Getter for <code>texera_db.workflow.last_modified_time</code>.
     */
    public Timestamp getLastModifiedTime();

    /**
     * Setter for <code>texera_db.workflow.eid</code>.
     */
    public void setEid(UInteger value);

    /**
     * Getter for <code>texera_db.workflow.eid</code>.
     */
    public UInteger getEid();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface IWorkflow
     */
    public void from(edu.uci.ics.texera.web.model.jooq.generated.tables.interfaces.IWorkflow from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface IWorkflow
     */
    public <E extends edu.uci.ics.texera.web.model.jooq.generated.tables.interfaces.IWorkflow> E into(E into);
}
