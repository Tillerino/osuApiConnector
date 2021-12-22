package org.tillerino.osuApiModel.deserializer;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * We store datetimes as longs and use this deserializer to deserialize ISO datetimes directly to longs
 * as an alternative to integer values.
 */
public class DateToLong extends StdDeserializer<Long> {
	public DateToLong() {
		super(TypeFactory.defaultInstance().constructSimpleType(long.class, null));
	}
	private static final long serialVersionUID = 1L;
	public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

	@Override
	public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
		if (p.currentToken() == JsonToken.VALUE_NUMBER_INT) {
			return p.getLongValue();
		}
		if (p.currentToken() == JsonToken.VALUE_STRING) {
			return ZonedDateTime.parse(p.getValueAsString(), dateTimeFormatter).toInstant().toEpochMilli();
		}
		throw MismatchedInputException.from(p, _valueType, "Expected a string or an integer");
	}

}
