/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.dao.jooq.generated.enums;


import org.jooq.Catalog;
import org.jooq.EnumType;
import org.jooq.Schema;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public enum DatasetUserAccessPrivilege implements EnumType {

    NONE("NONE"),

    READ("READ"),

    WRITE("WRITE");

    private final String literal;

    private DatasetUserAccessPrivilege(String literal) {
        this.literal = literal;
    }

    @Override
    public Catalog getCatalog() {
        return null;
    }

    @Override
    public Schema getSchema() {
        return null;
    }

    @Override
    public String getName() {
        return "dataset_user_access_privilege";
    }

    @Override
    public String getLiteral() {
        return literal;
    }
}
