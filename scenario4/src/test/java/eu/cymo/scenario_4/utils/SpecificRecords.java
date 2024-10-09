package eu.cymo.scenario_4.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.avro.specific.SpecificRecord;

public class SpecificRecords {

	private SpecificRecords() {}
	
	public static boolean isSpecificRecord(Type type) {
		if(type instanceof Class<?> cl) {
			return SpecificRecord.class.isAssignableFrom(cl);
		}
		if(type instanceof ParameterizedType pt) {
			return isSpecificRecord(pt.getRawType());
		}
		return false;
	}
	
}
