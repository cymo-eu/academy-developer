package eu.cymo.scenario_2.adapter.kafka;

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

@Configuration
public class ConsumerConfiguration {

	@Bean
	public ConsumerFactory<String, String> stringConsumerFactory(
			KafkaProperties kafkaProperties,
			ObjectProvider<SslBundles> sslBundles) {
		var properties = kafkaProperties.buildConsumerProperties(sslBundles.getIfAvailable());
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		return new DefaultKafkaConsumerFactory<>(properties);
	}
	
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> stringContainerFactory(
			ConsumerFactory<String, String> userConsumerFactory) {
		var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
		factory.setConsumerFactory(userConsumerFactory);
		return factory;
	}
	
}
