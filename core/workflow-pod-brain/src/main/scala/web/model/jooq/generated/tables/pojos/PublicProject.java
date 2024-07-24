/*
 * This file is generated by jOOQ.
 */
package web.model.jooq.generated.tables.pojos;


import org.jooq.types.UInteger;

import web.model.jooq.generated.tables.interfaces.IPublicProject;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PublicProject implements IPublicProject {

    private static final long serialVersionUID = -561108888;

    private UInteger pid;
    private UInteger uid;

    public PublicProject() {}

    public PublicProject(IPublicProject value) {
        this.pid = value.getPid();
        this.uid = value.getUid();
    }

    public PublicProject(
        UInteger pid,
        UInteger uid
    ) {
        this.pid = pid;
        this.uid = uid;
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
    public UInteger getUid() {
        return this.uid;
    }

    @Override
    public void setUid(UInteger uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PublicProject (");

        sb.append(pid);
        sb.append(", ").append(uid);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IPublicProject from) {
        setPid(from.getPid());
        setUid(from.getUid());
    }

    @Override
    public <E extends IPublicProject> E into(E into) {
        into.from(this);
        return into;
    }
}
