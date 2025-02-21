/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.dao.jooq.generated.tables.pojos;


import edu.uci.ics.texera.dao.jooq.generated.enums.PrivilegeEnum;
import edu.uci.ics.texera.dao.jooq.generated.tables.interfaces.IWorkflowUserAccess;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class WorkflowUserAccess implements IWorkflowUserAccess {

    private static final long serialVersionUID = 1L;

    private Integer       uid;
    private Integer       wid;
    private PrivilegeEnum privilege;

    public WorkflowUserAccess() {}

    public WorkflowUserAccess(IWorkflowUserAccess value) {
        this.uid = value.getUid();
        this.wid = value.getWid();
        this.privilege = value.getPrivilege();
    }

    public WorkflowUserAccess(
        Integer       uid,
        Integer       wid,
        PrivilegeEnum privilege
    ) {
        this.uid = uid;
        this.wid = wid;
        this.privilege = privilege;
    }

    /**
     * Getter for <code>texera_db.workflow_user_access.uid</code>.
     */
    @Override
    public Integer getUid() {
        return this.uid;
    }

    /**
     * Setter for <code>texera_db.workflow_user_access.uid</code>.
     */
    @Override
    public void setUid(Integer uid) {
        this.uid = uid;
    }

    /**
     * Getter for <code>texera_db.workflow_user_access.wid</code>.
     */
    @Override
    public Integer getWid() {
        return this.wid;
    }

    /**
     * Setter for <code>texera_db.workflow_user_access.wid</code>.
     */
    @Override
    public void setWid(Integer wid) {
        this.wid = wid;
    }

    /**
     * Getter for <code>texera_db.workflow_user_access.privilege</code>.
     */
    @Override
    public PrivilegeEnum getPrivilege() {
        return this.privilege;
    }

    /**
     * Setter for <code>texera_db.workflow_user_access.privilege</code>.
     */
    @Override
    public void setPrivilege(PrivilegeEnum privilege) {
        this.privilege = privilege;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("WorkflowUserAccess (");

        sb.append(uid);
        sb.append(", ").append(wid);
        sb.append(", ").append(privilege);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IWorkflowUserAccess from) {
        setUid(from.getUid());
        setWid(from.getWid());
        setPrivilege(from.getPrivilege());
    }

    @Override
    public <E extends IWorkflowUserAccess> E into(E into) {
        into.from(this);
        return into;
    }
}
