/*
 * This file is generated by jOOQ.
 */
package web.model.jooq.generated.tables.pojos;


import org.jooq.types.UInteger;

import web.model.jooq.generated.tables.interfaces.IWorkflowOfProject;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class WorkflowOfProject implements IWorkflowOfProject {

    private static final long serialVersionUID = -126247102;

    private UInteger wid;
    private UInteger pid;

    public WorkflowOfProject() {}

    public WorkflowOfProject(IWorkflowOfProject value) {
        this.wid = value.getWid();
        this.pid = value.getPid();
    }

    public WorkflowOfProject(
        UInteger wid,
        UInteger pid
    ) {
        this.wid = wid;
        this.pid = pid;
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
    public UInteger getPid() {
        return this.pid;
    }

    @Override
    public void setPid(UInteger pid) {
        this.pid = pid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("WorkflowOfProject (");

        sb.append(wid);
        sb.append(", ").append(pid);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IWorkflowOfProject from) {
        setWid(from.getWid());
        setPid(from.getPid());
    }

    @Override
    public <E extends IWorkflowOfProject> E into(E into) {
        into.from(this);
        return into;
    }
}
