package edu.uci.ics.texera.workflow.operators.dummy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import org.jooq.tools.StringUtils;

import java.util.Objects;

public class DummyProperty {
    @JsonProperty
    @JsonSchemaTitle("Dummy Attribute")
    @JsonPropertyDescription("Dummy Attribute for incompatible attribute")
    private String dummyAttribute;

    @JsonProperty
    @JsonSchemaTitle("Dummy Value")
    @JsonPropertyDescription("Value for the dummy attribute")
    private String dummyValue;

    DummyProperty (String dummyAttribute, String dummyValue) {
        this.dummyAttribute = dummyAttribute;
        this.dummyValue = dummyValue;
    }


    String getDummyAttribute(){
        return dummyAttribute;
    }


    String getDummyValue(){
        if(StringUtils.isBlank(dummyValue)){
            return dummyAttribute;
        }
        return dummyValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        edu.uci.ics.texera.workflow.operators.dummy.DummyProperty that = (edu.uci.ics.texera.workflow.operators.dummy.DummyProperty) o;
        return Objects.equals(dummyAttribute, that.dummyAttribute) && Objects.equals(dummyValue, that.dummyValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dummyAttribute, dummyValue);
    }
}
