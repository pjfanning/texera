/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.dao.jooq.generated.tables.pojos;


import edu.uci.ics.texera.dao.jooq.generated.tables.interfaces.IWorkflow;

import java.sql.Timestamp;

import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Workflow implements IWorkflow {

    private static final long serialVersionUID = 1973264418;

    private String    name;
    private String    description;
    private UInteger  wid;
    private String    content;
    private Timestamp creationTime;
    private Timestamp lastModifiedTime;
    private Byte      isPublic;

    public Workflow() {}

    public Workflow(IWorkflow value) {
        this.name = value.getName();
        this.description = value.getDescription();
        this.wid = value.getWid();
        this.content = value.getContent();
        this.creationTime = value.getCreationTime();
        this.lastModifiedTime = value.getLastModifiedTime();
        this.isPublic = value.getIsPublic();
    }

    public Workflow(
        String    name,
        String    description,
        UInteger  wid,
        String    content,
        Timestamp creationTime,
        Timestamp lastModifiedTime,
        Byte      isPublic
    ) {
        this.name = name;
        this.description = description;
        this.wid = wid;
        this.content = content;
        this.creationTime = creationTime;
        this.lastModifiedTime = lastModifiedTime;
        this.isPublic = isPublic;
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
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
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
    public String getContent() {
        return this.content;
    }

    @Override
    public void setContent(String content) {
        this.content = content;
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
    public Timestamp getLastModifiedTime() {
        return this.lastModifiedTime;
    }

    @Override
    public void setLastModifiedTime(Timestamp lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
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
    public String toString() {
        StringBuilder sb = new StringBuilder("Workflow (");

        sb.append(name);
        sb.append(", ").append(description);
        sb.append(", ").append(wid);
        sb.append(", ").append(content);
        sb.append(", ").append(creationTime);
        sb.append(", ").append(lastModifiedTime);
        sb.append(", ").append(isPublic);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IWorkflow from) {
        setName(from.getName());
        setDescription(from.getDescription());
        setWid(from.getWid());
        setContent(from.getContent());
        setCreationTime(from.getCreationTime());
        setLastModifiedTime(from.getLastModifiedTime());
        setIsPublic(from.getIsPublic());
    }

    @Override
    public <E extends IWorkflow> E into(E into) {
        into.from(this);
        return into;
    }
}
