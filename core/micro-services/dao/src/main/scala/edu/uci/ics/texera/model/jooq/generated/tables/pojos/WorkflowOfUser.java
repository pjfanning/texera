/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.model.jooq.generated.tables.pojos;


import edu.uci.ics.texera.model.jooq.generated.tables.interfaces.IWorkflowOfUser;

import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class WorkflowOfUser implements IWorkflowOfUser {

    private static final long serialVersionUID = 239550311;

    private UInteger uid;
    private UInteger wid;

    public WorkflowOfUser() {}

    public WorkflowOfUser(IWorkflowOfUser value) {
        this.uid = value.getUid();
        this.wid = value.getWid();
    }

    public WorkflowOfUser(
        UInteger uid,
        UInteger wid
    ) {
        this.uid = uid;
        this.wid = wid;
    }

    @Override
    public UInteger getUid() {
        return this.uid;
    }

    @Override
    public void setUid(UInteger uid) {
        this.uid = uid;
    }

    @Override
    public UInteger getWid() {
        return this.wid;
    }

    @Override
    public void setWid(UInteger wid) {
        this.wid = wid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("WorkflowOfUser (");

        sb.append(uid);
        sb.append(", ").append(wid);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IWorkflowOfUser from) {
        setUid(from.getUid());
        setWid(from.getWid());
    }

    @Override
    public <E extends IWorkflowOfUser> E into(E into) {
        into.from(this);
        return into;
    }
}
