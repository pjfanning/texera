/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.dao.jooq.generated.tables.pojos;


import edu.uci.ics.texera.dao.jooq.generated.enums.DatasetUserAccessPrivilege;
import edu.uci.ics.texera.dao.jooq.generated.tables.interfaces.IDatasetUserAccess;

import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DatasetUserAccess implements IDatasetUserAccess {

    private static final long serialVersionUID = -529435584;

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
