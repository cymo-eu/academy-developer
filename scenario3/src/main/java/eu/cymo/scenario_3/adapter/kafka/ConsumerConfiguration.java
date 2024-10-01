package eu.cymo.scenario_3.adapter.kafka;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;

@Configuration
public class ConsumerConfiguration {

	@Bean
	public ConsumerFactory<String, SpecificRecord> specificRecordConsumerFactory(
			KafkaProperties kafkaProperties,
			ObjectProvider<SslBundles> sslBundles) {
		var properties = kafkaProperties.buildConsumerProperties(sslBundles.getIfAvailable());
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SpecificAvroDeserializer.class);
		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		return new DefaultKafkaConsumerFactory<>(properties);
	}
	
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, SpecificRecord> specificRecordContainerFactory(
			ConsumerFactory<String, SpecificRecord> userConsumerFactory) {
		var factory = new ConcurrentKafkaListenerContainerFactory<String, SpecificRecord>();
		factory.setConsumerFactory(userConsumerFactory);
		return factory;
	}
	
}
