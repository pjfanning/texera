package edu.uci.ics.texera.workflow.operators.source.scan.text;

import com.fasterxml.jackson.annotation.JsonValue;
import edu.uci.ics.texera.workflow.common.tuple.schema.AttributeType;

public enum TextInputSourceAttributeType {
    /**
     * TextIinputSourceAttributeType has all the possible types of the output column for the TextInputSource operator.
     * It is modeled from AttributeType but modified for the specific use case of this operator.
     *
     * All text source operators only output tuples with one field
     * All attribute types except ANY and BINARY are supported
     * **/

    STRING("string",AttributeType.STRING),
    // this replaces the previous "outputAsSingleTuple" mode, which only applies to STRING
    STRING_AS_SINGLE_TUPLE("string (entire input in 1 tuple)", AttributeType.STRING),
    INTEGER("integer", AttributeType.INTEGER),
    LONG("long", AttributeType.LONG),
    DOUBLE("double", AttributeType.DOUBLE),
    BOOLEAN("boolean", AttributeType.BOOLEAN),
    TIMESTAMP("timestamp", AttributeType.TIMESTAMP);

    private final String name;
    private final AttributeType type;

    TextInputSourceAttributeType(String name, AttributeType type) {
        this.name = name;
        this.type = type;
    }

    @JsonValue
    public String getName() { return this.name; }

    public AttributeType getType() { return this.type; }

    @Override
    public String toString() {
        return this.getName();
    }

    public boolean isOutputSingleTuple() { return this == TextInputSourceAttributeType.STRING_AS_SINGLE_TUPLE; }
}