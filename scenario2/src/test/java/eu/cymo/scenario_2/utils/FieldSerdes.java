package eu.cymo.scenario_2.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;

public class FieldSerdes {

	private FieldSerdes() {}

	public static Serde<?> getKeySerde(Field field, Map<String, ?> config) {
		return getSerde(field, config, true);
	}

	public static Serde<?> getValueSerde(Field field, Map<String, ?> config) {
		return getSerde(field, config, false);
	}
	
	public static Serde<?> getSerde(Field field, Map<String, ?> config, boolean isKey) {
		var type = isKey ? getKeyType(field) : getValueType(field);
		
		if(type == String.class) {
			return Serdes.String();
		}
		if(type == GenericRecord.class) {
			var serdes = new GenericAvroSerde();
			serdes.configure(config, isKey);
			return serdes;
		}
		throw new IllegalArgumentException("No serdes defined for type '%s'".formatted(type));
	}

	private static Type getKeyType(Field field) {
		return Fields.getParemeterizdTypes(field)[0];
	}

	private static Type getValueType(Field field) {
		return Fields.getParemeterizdTypes(field)[1];
	}
}
