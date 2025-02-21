/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.dao.jooq.generated.tables.interfaces;


import java.io.Serializable;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IWorkflowViewCount extends Serializable {

    /**
     * Setter for <code>texera_db.workflow_view_count.wid</code>.
     */
    public void setWid(Integer value);

    /**
     * Getter for <code>texera_db.workflow_view_count.wid</code>.
     */
    public Integer getWid();

    /**
     * Setter for <code>texera_db.workflow_view_count.view_count</code>.
     */
    public void setViewCount(Integer value);

    /**
     * Getter for <code>texera_db.workflow_view_count.view_count</code>.
     */
    public Integer getViewCount();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common
     * interface IWorkflowViewCount
     */
    public void from(IWorkflowViewCount from);

    /**
     * Copy data into another generated Record/POJO implementing the common
     * interface IWorkflowViewCount
     */
    public <E extends IWorkflowViewCount> E into(E into);
}
