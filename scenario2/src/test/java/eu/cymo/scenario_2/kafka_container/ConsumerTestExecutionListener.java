package eu.cymo.scenario_2.kafka_container;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

import eu.cymo.scenario_2.utils.FieldSerdes;
import eu.cymo.scenario_2.utils.Fields;

public class ConsumerTestExecutionListener implements TestExecutionListener {
	private static final String CONSUMERS = "consumers";

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		for(var field: Fields.getAllFields(testContext.getTestClass())) {
			if(supportsField(field)) {
				var fieldInstance = Fields.getFieldsInstance(field, testContext.getTestInstance());
				
				field.setAccessible(true);
				field.set(fieldInstance, resolveField(field, testContext));
			}
		}
	}
	
	private boolean supportsField(Field field) {
		return isConsumer(field) || isConsumerAssert(field);
	}
	
	private boolean isConsumer(Field field) {
		return field.getType() == Consumer.class &&
				field.isAnnotationPresent(TestConsumer.class);
	}

	private boolean isConsumerAssert(Field field) {
		return field.getType() == ConsumerAssert.class &&
				field.isAnnotationPresent(TestConsumer.class);
	}
	
	private Object resolveField(Field field, TestContext testContext) {
		if(isConsumer(field)) {
			return createConsumer(field, testContext);
		}
		if(isConsumerAssert(field)) {
			return new ConsumerAssert<>(createConsumer(field, testContext));
		}
		return null;
	}
	
	private Consumer<?, ?> createConsumer(Field field, TestContext testContext) {
		var consumer = new KafkaConsumer<>(
				consumerConfig(field, testContext),
				FieldSerdes.getKeySerde(field, consumerConfig(field, testContext)).deserializer(),
				FieldSerdes.getValueSerde(field, consumerConfig(field, testContext)).deserializer());
		consumer.subscribe(Collections.singletonList(topic(field, testContext)));
		
		return registerConsumer(testContext, consumer);
	}
	
	private String topic(Field field, TestContext testContext) {
		var applicationContext = testContext.getApplicationContext();
		
		var environment = applicationContext.getBean(Environment.class);
		
		return environment.resolvePlaceholders(field.getAnnotation(TestConsumer.class).topic());
	}
	
	private Map<String, Object> consumerConfig(Field field, TestContext testContext) {
		var applicationContext = testContext.getApplicationContext();
		
		var kafkaProperties = applicationContext.getBean(KafkaProperties.class);
		var sslBundles = applicationContext.getBeanProvider(SslBundles.class);
		
		var properties = kafkaProperties.buildConsumerProperties(sslBundles.getIfAvailable());
		
		var testConsumer = field.getAnnotation(TestConsumer.class);
		var groupId = testConsumer.groupId();
		if(!groupId.isBlank()) {
			properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		}
		properties.putAll(additionalProperites(testConsumer));
		return properties;
	}
	
	private Map<String, Object> additionalProperites(TestConsumer testConsumer) {
		return Stream.of(testConsumer.properties())
				.map(s -> s.split("="))
				.collect(Collectors.toMap(s -> s[0], s -> s[1]));
	}
	
	private Consumer<?, ?> registerConsumer(TestContext testContext, Consumer<?, ?> consumer) {
		testContext.computeAttribute(CONSUMERS, s -> new ArrayList<Consumer<?, ?>>()).add(consumer);
		return consumer;
	}
	
	@Override
	public void afterTestMethod(TestContext testContext) throws Exception {
		testContext.computeAttribute(CONSUMERS, s -> new ArrayList<Consumer<?, ?>>())
			.forEach(Consumer::close);
		testContext.removeAttribute(CONSUMERS);
	}
}
