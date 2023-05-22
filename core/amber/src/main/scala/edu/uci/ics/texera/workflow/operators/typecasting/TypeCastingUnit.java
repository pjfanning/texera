package edu.uci.ics.texera.workflow.operators.typecasting;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName;
import edu.uci.ics.texera.workflow.common.tuple.schema.AttributeType;

@JsonSchemaInject(json = "{\n" +
                         "  \"attributeType1\": {\n" +
                         "    \"attribute\": {\n" +
                         "      \"allOf\": [\n" +
                         "        {\n" +
                         "          \"if\": {\n" +
                         "            \"resultType\": {\n" +
                         "              \"enum\": [\"integer\"]\n" +
                         "            }\n" +
                         "          },\n" +
                         "          \"then\": {\n" +
                         "            \"enum\": [\"string\", \"integer\", \"long\", \"double\", \"boolean\"]\n" +
                         "          }\n" +
                         "        },\n" +
                         "        {\n" +
                         "          \"if\": {\n" +
                         "            \"resultType\": {\n" +
                         "              \"enum\": [\"long\"]\n" +
                         "            }\n" +
                         "          },\n" +
                         "          \"then\": {\n" +
                         "            \"enum\": [\"string\", \"integer\", \"long\", \"double\", \"boolean\", \"timestamp\"]\n" +
                         "          }\n" +
                         "        },\n" +
                         "        {\n" +
                         "          \"if\": {\n" +
                         "            \"resultType\": {\n" +
                         "              \"enum\": [\"timestamp\"]\n" +
                         "            }\n" +
                         "          },\n" +
                         "          \"then\": {\n" +
                         "            \"enum\": [\"string\", \"long\", \"timestamp\"]\n" +
                         "          }\n" +
                         "        },\n" +
                         "        {\n" +
                         "          \"if\": {\n" +
                         "            \"resultType\": {\n" +
                         "              \"enum\": [\"double\"]\n" +
                         "            }\n" +
                         "          },\n" +
                         "          \"then\": {\n" +
                         "            \"enum\": [\"string\", \"integer\", \"long\", \"double\", \"boolean\"]\n" +
                         "          }\n" +
                         "        },\n" +
                         "        {\n" +
                         "          \"if\": {\n" +
                         "            \"resultType\": {\n" +
                         "              \"enum\": [\"boolean\"]\n" +
                         "            }\n" +
                         "          },\n" +
                         "          \"then\": {\n" +
                         "            \"enum\": [\"string\", \"integer\", \"long\", \"double\", \"boolean\"]\n" +
                         "          }\n" +
                         "        }\n" +
                         "      ]\n" +
                         "    }\n" +
                         "  }\n" +
                         "}")
public class TypeCastingUnit {
    @JsonProperty(required = true)
    @JsonSchemaTitle("Attribute")
    @JsonPropertyDescription("Attribute for type casting")
    @AutofillAttributeName
    public String attribute;

    @JsonProperty(required = true)
    @JsonSchemaTitle("Cast type")
    @JsonPropertyDescription("Result type after type casting")
    public AttributeType resultType;

    //TODO: override equals to pass equality check for typecasting operator during cache status update
}
