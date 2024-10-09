package eu.cymo.scenario_1.utils;

import java.util.Optional;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;

public class Headers {
	
	public static final String EVENT_TYPE = "EventType";

	private Headers() {}
	
	public static Header eventType(String eventType) {
		return header(EVENT_TYPE, eventType);
	}
	
	public static Header header(String key, String value) {
		return new RecordHeader(key, value.getBytes());
	}
	
	public static String eventType(org.apache.kafka.common.header.Headers headers) {
		return Optional.ofNullable(headers.lastHeader(EVENT_TYPE))
				.map(Header::value)
				.map(String::new)
				.orElse(null);
	}
	
}
