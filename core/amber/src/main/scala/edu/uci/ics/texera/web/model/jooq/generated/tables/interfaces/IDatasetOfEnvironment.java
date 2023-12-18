/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.web.model.jooq.generated.tables.interfaces;


import java.io.Serializable;

import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IDatasetOfEnvironment extends Serializable {

    /**
     * Setter for <code>texera_db.dataset_of_environment.did</code>.
     */
    public void setDid(UInteger value);

    /**
     * Getter for <code>texera_db.dataset_of_environment.did</code>.
     */
    public UInteger getDid();

    /**
     * Setter for <code>texera_db.dataset_of_environment.eid</code>.
     */
    public void setEid(UInteger value);

    /**
     * Getter for <code>texera_db.dataset_of_environment.eid</code>.
     */
    public UInteger getEid();

    /**
     * Setter for <code>texera_db.dataset_of_environment.version_descriptor</code>.
     */
    public void setVersionDescriptor(String value);

    /**
     * Getter for <code>texera_db.dataset_of_environment.version_descriptor</code>.
     */
    public String getVersionDescriptor();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface IDatasetOfEnvironment
     */
    public void from(edu.uci.ics.texera.web.model.jooq.generated.tables.interfaces.IDatasetOfEnvironment from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface IDatasetOfEnvironment
     */
    public <E extends edu.uci.ics.texera.web.model.jooq.generated.tables.interfaces.IDatasetOfEnvironment> E into(E into);
}
