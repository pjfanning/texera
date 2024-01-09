/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.web.model.jooq.generated.tables.pojos;


import edu.uci.ics.texera.web.model.jooq.generated.tables.interfaces.IDatasetVersion;

import java.sql.Timestamp;

import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DatasetVersion implements IDatasetVersion {

    private static final long serialVersionUID = 797186246;

    private UInteger  dvid;
    private UInteger  did;
    private String    name;
    private String    versionHash;
    private Timestamp creationTime;

    public DatasetVersion() {}

    public DatasetVersion(IDatasetVersion value) {
        this.dvid = value.getDvid();
        this.did = value.getDid();
        this.name = value.getName();
        this.versionHash = value.getVersionHash();
        this.creationTime = value.getCreationTime();
    }

    public DatasetVersion(
        UInteger  dvid,
        UInteger  did,
        String    name,
        String    versionHash,
        Timestamp creationTime
    ) {
        this.dvid = dvid;
        this.did = did;
        this.name = name;
        this.versionHash = versionHash;
        this.creationTime = creationTime;
    }

    @Override
    public UInteger getDvid() {
        return this.dvid;
    }

    @Override
    public void setDvid(UInteger dvid) {
        this.dvid = dvid;
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
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getVersionHash() {
        return this.versionHash;
    }

    @Override
    public void setVersionHash(String versionHash) {
        this.versionHash = versionHash;
    }

    @Override
    public Timestamp getCreationTime() {
        return this.creationTime;
    }

    @Override
    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DatasetVersion (");

        sb.append(dvid);
        sb.append(", ").append(did);
        sb.append(", ").append(name);
        sb.append(", ").append(versionHash);
        sb.append(", ").append(creationTime);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IDatasetVersion from) {
        setDvid(from.getDvid());
        setDid(from.getDid());
        setName(from.getName());
        setVersionHash(from.getVersionHash());
        setCreationTime(from.getCreationTime());
    }

    @Override
    public <E extends IDatasetVersion> E into(E into) {
        into.from(this);
        return into;
    }
}
