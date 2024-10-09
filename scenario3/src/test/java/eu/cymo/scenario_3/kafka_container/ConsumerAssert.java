package eu.cymo.scenario_3.kafka_container;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.assertj.core.api.ThrowingConsumer;
import org.awaitility.Awaitility;

public class ConsumerAssert<K, V> {
	private Consumer<K, V> consumer;
	
	public ConsumerAssert(
			Consumer<K, V> consumer) {
		this.consumer = consumer;
	}
	
	public void verify(ThrowingConsumer<List<ConsumerRecord<K, V>>> assertion) {
		var records = new ArrayList<ConsumerRecord<K, V>>();
		
		Awaitility.await()
			.untilAsserted(() -> {
				consumer.poll(Duration.of(100, ChronoUnit.MILLIS))
					.forEach(records::add);
				
				assertion.accept(records);
			});
	}
	
	public void verifyAsMap(ThrowingConsumer<Map<K, V>> assertion) {
		verify(records -> {
			assertion.accept(records.stream()
					.collect(Collectors.toMap(
							ConsumerRecord::key, 
							Function.identity(),
							(left, right) -> left.timestamp() >= right.timestamp() ? left : right)) // filter out duplicate keys and key latest
					.entrySet()
					.stream()
					.collect(Collectors.toMap(Entry::getKey, e -> e.getValue().value())));
		});
	}
}
