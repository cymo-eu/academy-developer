package eu.cymo.scenario_7.kafka_topology;

import java.lang.reflect.Field;

import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

import eu.cymo.scenario_7.adapter.kafka.AvroSerdeFactory;
import eu.cymo.scenario_7.utils.FieldSerdes;
import eu.cymo.scenario_7.utils.Fields;

public class TestTopicTestExecutionListener implements TestExecutionListener {

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		for(var field : testContext.getTestClass().getDeclaredFields()) {
			if(supportsField(field)) {
				field.setAccessible(true);
				field.set(testContext.getTestInstance(), resolveField(field, testContext));
			}
		}
	}
	
	private boolean supportsField(Field field) {
		return isTestInputTopic(field) || isTestOutputTopic(field);
	}
	
	private boolean isTestInputTopic(Field field) {
		return field.isAnnotationPresent(TestTopic.class) &&
				field.getType() == TestInputTopic.class;
	}
	
	private boolean isTestOutputTopic(Field field) {
		return field.isAnnotationPresent(TestTopic.class) &&
				field.getType() == TestOutputTopic.class;
	}
	
	private Object resolveField(Field field, TestContext testContext) {
		if(isTestInputTopic(field)) {
			return createInputTopic(field, testContext);
		}
		if(isTestOutputTopic(field)) {
			return createOutputTopic(field, testContext);
		}
		return null;
	}
	
	private TestInputTopic<?, ?> createInputTopic(Field field, TestContext testContext) {
		return topologyTestDriver(testContext)
				.createInputTopic(
						topic(field, testContext),
						FieldSerdes.getKeySerde(field, serdeFactory(testContext)).serializer(),
						FieldSerdes.getValueSerde(field, serdeFactory(testContext)).serializer());
	}
	
	private TestOutputTopic<?, ?> createOutputTopic(Field field, TestContext testContext) {
		return topologyTestDriver(testContext)
				.createOutputTopic(
						topic(field, testContext),
						FieldSerdes.getSerde(field, serdeFactory(testContext), true).deserializer(),
						FieldSerdes.getSerde(field, serdeFactory(testContext), false).deserializer());
	}
	
	private String topic(Field field, TestContext testContext) {
		var applicationContext = testContext.getApplicationContext();
		
		var environment = applicationContext.getBean(Environment.class);
		
		return environment.resolvePlaceholders(Fields.getAnnotationValue(field, TestTopic.class, TestTopic::value));
	}
	
	private TopologyTestDriver topologyTestDriver(TestContext testContext) {
		return (TopologyTestDriver) testContext.getAttribute(TopologyTestDriverTestExecutionListener.TOPOLOGY_TEST_DRIVER);
	}
	
	private AvroSerdeFactory serdeFactory(TestContext testContext) {
		return testContext.getApplicationContext().getBean(AvroSerdeFactory.class);
	}
}
