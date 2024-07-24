/*
 * This file is generated by jOOQ.
 */
package web.model.jooq.generated.enums;


import org.jooq.Catalog;
import org.jooq.EnumType;
import org.jooq.Schema;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public enum ProjectUserAccessPrivilege implements EnumType {

    NONE("NONE"),

    READ("READ"),

    WRITE("WRITE");

    private final String literal;

    private ProjectUserAccessPrivilege(String literal) {
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
        return "project_user_access_privilege";
    }

    @Override
    public String getLiteral() {
        return literal;
    }
}
