package org.tillerino.osuApiModel.deserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.junit.Test;

public class DateToLongTest {
    @Data
    static class ClassWithLongDateField {
        @JsonDeserialize(using = DateToLong.class)
        private long dateField;
    }

    ObjectMapper jackson = new ObjectMapper();

    @Test
    public void testStringDateDeserialization() throws Exception {
        ClassWithLongDateField readValue =
                jackson.readValue("{ \"dateField\": \"1970-01-01 00:00:01\" }", ClassWithLongDateField.class);

        assertThat(readValue).hasFieldOrPropertyWithValue("dateField", 1000L);
    }

    /**
     * We test that we can deserialize the field just like it is serialized.
     */
    @Test
    public void roundTrip() throws Exception {
        ClassWithLongDateField value = new ClassWithLongDateField();
        value.setDateField(1000L);

        assertThat(jackson.readValue(jackson.writeValueAsString(value), ClassWithLongDateField.class))
                .isEqualTo(value);
    }

    @Test
    public void unexpectedFieldType() throws Exception {
        assertThatThrownBy(() -> jackson.readValue("{ \"dateField\": true }", ClassWithLongDateField.class))
                .hasMessageContaining("Expected a string or an integer")
                // nice Jackson error containing the path
                .hasMessageContaining("ClassWithLongDateField[\"dateField\"]");
    }
}
