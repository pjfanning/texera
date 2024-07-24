/*
 * This file is generated by jOOQ.
 */
package web.model.jooq.generated.tables.pojos;


import org.jooq.types.UInteger;

import web.model.jooq.generated.enums.DatasetUserAccessPrivilege;
import web.model.jooq.generated.tables.interfaces.IDatasetUserAccess;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DatasetUserAccess implements IDatasetUserAccess {

    private static final long serialVersionUID = 1774067398;

    private UInteger                   did;
    private UInteger                   uid;
    private DatasetUserAccessPrivilege privilege;

    public DatasetUserAccess() {}

    public DatasetUserAccess(IDatasetUserAccess value) {
        this.did = value.getDid();
        this.uid = value.getUid();
        this.privilege = value.getPrivilege();
    }

    public DatasetUserAccess(
        UInteger                   did,
        UInteger                   uid,
        DatasetUserAccessPrivilege privilege
    ) {
        this.did = did;
        this.uid = uid;
        this.privilege = privilege;
    }

    @Override
    public UInteger getDid() {
        return this.did;
    }

    @Override
    public void setDid(UInteger did) {
        this.did = did;
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
    public DatasetUserAccessPrivilege getPrivilege() {
        return this.privilege;
    }

    @Override
    public void setPrivilege(DatasetUserAccessPrivilege privilege) {
        this.privilege = privilege;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DatasetUserAccess (");

        sb.append(did);
        sb.append(", ").append(uid);
        sb.append(", ").append(privilege);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IDatasetUserAccess from) {
        setDid(from.getDid());
        setUid(from.getUid());
        setPrivilege(from.getPrivilege());
    }

    @Override
    public <E extends IDatasetUserAccess> E into(E into) {
        into.from(this);
        return into;
    }
}
