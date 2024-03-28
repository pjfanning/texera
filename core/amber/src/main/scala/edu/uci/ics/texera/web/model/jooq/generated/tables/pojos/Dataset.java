/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.web.model.jooq.generated.tables.pojos;


import edu.uci.ics.texera.web.model.jooq.generated.tables.interfaces.IDataset;

import java.sql.Timestamp;

import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Dataset implements IDataset {

    private static final long serialVersionUID = 178066411;

    private UInteger  did;
    private UInteger  ownerUid;
    private String    name;
    private Byte      isPublic;
    private String    description;
    private Timestamp creationTime;

    public Dataset() {}

    public Dataset(IDataset value) {
        this.did = value.getDid();
        this.ownerUid = value.getOwnerUid();
        this.name = value.getName();
        this.isPublic = value.getIsPublic();
        this.description = value.getDescription();
        this.creationTime = value.getCreationTime();
    }

    public Dataset(
        UInteger  did,
        UInteger  ownerUid,
        String    name,
        Byte      isPublic,
        String    description,
        Timestamp creationTime
    ) {
        this.did = did;
        this.ownerUid = ownerUid;
        this.name = name;
        this.isPublic = isPublic;
        this.description = description;
        this.creationTime = creationTime;
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
    public UInteger getOwnerUid() {
        return this.ownerUid;
    }

    @Override
    public void setOwnerUid(UInteger ownerUid) {
        this.ownerUid = ownerUid;
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
    public Byte getIsPublic() {
        return this.isPublic;
    }

    @Override
    public void setIsPublic(Byte isPublic) {
        this.isPublic = isPublic;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
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
        StringBuilder sb = new StringBuilder("Dataset (");

        sb.append(did);
        sb.append(", ").append(ownerUid);
        sb.append(", ").append(name);
        sb.append(", ").append(isPublic);
        sb.append(", ").append(description);
        sb.append(", ").append(creationTime);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IDataset from) {
        setDid(from.getDid());
        setOwnerUid(from.getOwnerUid());
        setName(from.getName());
        setIsPublic(from.getIsPublic());
        setDescription(from.getDescription());
        setCreationTime(from.getCreationTime());
    }

    @Override
    public <E extends IDataset> E into(E into) {
        into.from(this);
        return into;
    }
}
