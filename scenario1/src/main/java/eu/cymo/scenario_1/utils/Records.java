package eu.cymo.scenario_1.utils;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public class Records {

	private Records() { }
	
	public static String readableString(ConsumerRecord<? ,?> record) {
		return "ConsumerRecord[topic='%s', partition='%s', offset='%']".formatted(
				record.topic(),
				record.partition(),
				record.offset());
	}
}
