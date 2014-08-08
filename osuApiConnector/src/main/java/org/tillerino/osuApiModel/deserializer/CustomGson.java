package org.tillerino.osuApiModel.deserializer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class CustomGson<T> implements JsonDeserializer<T> {
	final private Gson delegate;
	final Set<String> fields = new HashSet<>();
	final List<String> dateFields = new ArrayList<>();
	final boolean throwOnPropertyNotCovered;
	public static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.forOffsetHours(8));

	private CustomGson(Class<?> baseCls, final Class<?> instanceClass, boolean throwOnPropertyNotCovered) {
		super();
		this.throwOnPropertyNotCovered = throwOnPropertyNotCovered;
		GsonBuilder delegateBuilder = new GsonBuilder();
		delegateBuilder.addDeserializationExclusionStrategy(new ExclusionStrategy() {
			@Override
			public boolean shouldSkipField(FieldAttributes f) {
				return f.getAnnotation(Skip.class) != null;
			}
			
			@Override
			public boolean shouldSkipClass(Class<?> clazz) {
				return false;
			}
		});
		delegateBuilder.registerTypeAdapter(baseCls, new InstanceCreator<Object>() {
			@Override
			public Object createInstance(Type type) {
				try {
					return instanceClass.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException();
				}
			}
		});
		this.delegate = delegateBuilder.create();
		for(; !baseCls.equals(Object.class); baseCls = baseCls.getSuperclass()) {
			Set<String> fieldsFromThisClass = new HashSet<>();
			Set<String> dateFieldsFromThisClass = new HashSet<>();
			
			Field[] originalFields = baseCls.getDeclaredFields();
			for (int i = 0; i < originalFields.length; i++) {
				Field field = originalFields[i];
				if(field.isAnnotationPresent(Skip.class)) {
					continue;
				}
				int modifiers = field.getModifiers();
				if(Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
					continue;
				}
				
				String name = field.getName();
				
				if(field.isAnnotationPresent(SerializedName.class)) {
					name = field.getAnnotation(SerializedName.class).value();
				}
				
				fieldsFromThisClass.add(name);
				
				if(field.isAnnotationPresent(Date.class)) {
					dateFieldsFromThisClass.add(name);
				}
			}
			
			fields.addAll(fieldsFromThisClass);
			dateFields.addAll(dateFieldsFromThisClass);
		}
	}

	@Override
	public T deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject o = (JsonObject) json;
		for(String f : fields) {
			if(!o.has(f)) {
				throw new RuntimeException("missing field " + f);
			}
		}
		
		if(throwOnPropertyNotCovered) {
			for(Entry<String, ?> e : o.entrySet()) {
				if(!fields.contains(e.getKey())) {
					throw new RuntimeException("class does not cover property: " + e.getKey());
				}
			}
		}
		
		for(String dateField : dateFields) {
			JsonElement jsonElement = o.get(dateField);
			if(!jsonElement.isJsonNull()) {
				o.addProperty(dateField, CustomGson.dateTimeFormatter.parseDateTime(jsonElement.getAsString()).getMillis());
			}
		}

		return delegate.fromJson(json, typeOfT);
	}

	public static Gson wrap(final boolean throwOnPropertyNotCovered, Class<?>... classes) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		for (int i = 0; i < classes.length; i++) {
			final Class<?> baseCls = classes[i];
			gsonBuilder.registerTypeAdapterFactory(new TypeAdapterFactory() {
				@Override
				public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> requestedType) {
					if(!baseCls.isAssignableFrom(requestedType.getRawType())) {
						return null;
					}
					
					GsonBuilder builderForThisType = new GsonBuilder();
					builderForThisType.registerTypeAdapter(requestedType.getType(), new CustomGson<>(baseCls, requestedType.getRawType(), throwOnPropertyNotCovered));
					
					final Gson gsonForThisType = builderForThisType.create();
					
					return new TypeAdapter<T>() {
						@Override
						public void write(JsonWriter out, T value)
								throws IOException {
							gsonForThisType.toJson(value, baseCls, out);
						}

						@Override
						public T read(JsonReader in) throws IOException {
							return gsonForThisType.fromJson(in, requestedType.getType());
						}
					};
				}
			});
		}
		return gsonBuilder.create();
	}
}
