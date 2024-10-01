package eu.cymo.scenario_3.kafka_container;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

import eu.cymo.scenario_3.utils.FieldSerdes;
import eu.cymo.scenario_3.utils.Fields;

public class ProducerTestExecutionListener implements TestExecutionListener {
	private static final String PRODUCERS = "producers";

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		var producers = testContext.computeAttribute(PRODUCERS, s -> new ArrayList<Producer<?, ?>>());

		for(var field: Fields.getAllFields(testContext.getTestClass())) {
			if(supportsField(field)) {
				var fieldInstance = Fields.getFieldsInstance(field, testContext.getTestInstance());

				var producer = resolveField(field, testContext);
				producers.add(producer);
				
				field.setAccessible(true);
				field.set(fieldInstance, producer);
			}
		}
	}
	
	private boolean supportsField(Field field) {
		return field.getType() == Producer.class;
	}
	
	private Producer<?, ?> resolveField(Field field, TestContext testContext) {
		return new KafkaProducer<>(
				producerConfig(testContext),
				FieldSerdes.getKeySerde(field, producerConfig(testContext)).serializer(),
				FieldSerdes.getValueSerde(field, producerConfig(testContext)).serializer());
	}
	
	private Map<String, Object> producerConfig(TestContext testContext) {
		var applicationContext = testContext.getApplicationContext();
		
		var kafkaProperties = applicationContext.getBean(KafkaProperties.class);
		var sslBundles = applicationContext.getBeanProvider(SslBundles.class);
		
		return kafkaProperties.buildProducerProperties(sslBundles.getIfAvailable());
	}
	
	@Override
	public void afterTestMethod(TestContext testContext) throws Exception {
		testContext.computeAttribute(PRODUCERS, s -> new ArrayList<Producer<?, ?>>())
			.forEach(Producer::close);
		testContext.removeAttribute(PRODUCERS);
	}
}
