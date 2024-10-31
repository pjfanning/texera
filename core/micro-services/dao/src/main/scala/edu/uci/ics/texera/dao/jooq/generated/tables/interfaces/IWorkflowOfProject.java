/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.dao.jooq.generated.tables.interfaces;


import java.io.Serializable;

import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IWorkflowOfProject extends Serializable {

    /**
     * Setter for <code>texera_db.workflow_of_project.wid</code>.
     */
    public void setWid(UInteger value);

    /**
     * Getter for <code>texera_db.workflow_of_project.wid</code>.
     */
    public UInteger getWid();

    /**
     * Setter for <code>texera_db.workflow_of_project.pid</code>.
     */
    public void setPid(UInteger value);

    /**
     * Getter for <code>texera_db.workflow_of_project.pid</code>.
     */
    public UInteger getPid();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface IWorkflowOfProject
     */
    public void from(edu.uci.ics.texera.dao.jooq.generated.tables.interfaces.IWorkflowOfProject from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface IWorkflowOfProject
     */
    public <E extends edu.uci.ics.texera.dao.jooq.generated.tables.interfaces.IWorkflowOfProject> E into(E into);
}
