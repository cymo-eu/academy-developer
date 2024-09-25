package eu.cymo.scenario_1.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class FieldSerdes {

	private FieldSerdes() {}

	public static Serde<?> getKeySerde(Field field) {
		return getSerde(field, true);
	}

	public static Serde<?> getValueSerde(Field field) {
		return getSerde(field, false);
	}
	
	public static Serde<?> getSerde(Field field, boolean isKey) {
		var type = isKey ? getKeyType(field) : getValueType(field);
		
		if(type == String.class) {
			return Serdes.String();
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
