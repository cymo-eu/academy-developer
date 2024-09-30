package eu.cymo.scenario_2.adapter.kafka;

import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;

import eu.cymo.scenario_2.utils.Headers;

public class ProducerRecordBuilder<K, V> {
	private String topic;
	private K key;
	private V value;
	private List<Header> headers = new ArrayList<>();
	
	private ProducerRecordBuilder() {}
	
	public ProducerRecordBuilder<K, V> topic(String topic) {
		this.topic = topic;
		return this;
	}
	
	public ProducerRecordBuilder<K, V> key(K key) {
		this.key = key;
		return this;
	}
	
	public ProducerRecordBuilder<K, V> value(V value) {
		this.value = value;
		return this;
	}
	
	public ProducerRecordBuilder<K, V> eventType(String eventType) {
		headers.add(Headers.eventType(eventType));
		return this;
	}
	
	public ProducerRecordBuilder<K, V> header(String key, String value) {
		headers.add(Headers.header(key, value));
		return this;
	}
	
	public ProducerRecord<K, V> build() {
		return new ProducerRecord<>(topic, null, key, value, headers);
	}
	
	public static <K, V> ProducerRecordBuilder<K, V> newBuilder() {
		return new ProducerRecordBuilder<>();
	}
}
