/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.dao.jooq.generated.tables.interfaces;


import java.io.Serializable;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IDatasetViewCount extends Serializable {

    /**
     * Setter for <code>texera_db.dataset_view_count.did</code>.
     */
    public void setDid(Integer value);

    /**
     * Getter for <code>texera_db.dataset_view_count.did</code>.
     */
    public Integer getDid();

    /**
     * Setter for <code>texera_db.dataset_view_count.view_count</code>.
     */
    public void setViewCount(Integer value);

    /**
     * Getter for <code>texera_db.dataset_view_count.view_count</code>.
     */
    public Integer getViewCount();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common
     * interface IDatasetViewCount
     */
    public void from(IDatasetViewCount from);

    /**
     * Copy data into another generated Record/POJO implementing the common
     * interface IDatasetViewCount
     */
    public <E extends IDatasetViewCount> E into(E into);
}
