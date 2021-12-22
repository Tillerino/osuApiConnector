package org.tillerino.osuApiModel.deserializer;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * We store datetimes as longs and use this deserializer to deserialize ISO datetimes directly to longs.
 */
public class DateToLong extends StdDeserializer<Long> {
	public DateToLong() {
		super(TypeFactory.defaultInstance().constructSimpleType(long.class, null));
	}
	private static final long serialVersionUID = 1L;
	public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

	@Override
	public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
		String valueAsString = p.getValueAsString();
		return ZonedDateTime.parse(valueAsString, dateTimeFormatter).toInstant().toEpochMilli();
	}

}
