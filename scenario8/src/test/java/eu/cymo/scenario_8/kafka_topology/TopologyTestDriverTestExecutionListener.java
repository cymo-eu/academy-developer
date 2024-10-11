package eu.cymo.scenario_8.kafka_topology;

import java.time.Instant;
import java.util.Properties;

import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

public class TopologyTestDriverTestExecutionListener implements TestExecutionListener {
	public static final String TOPOLOGY_TEST_DRIVER = "topologyTestDriver";

	@SuppressWarnings("resource")
	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		var applicationContext = testContext.getApplicationContext();
		
		var driver = testContext.computeAttribute(TOPOLOGY_TEST_DRIVER, ttd -> createTestDriver(applicationContext));
		
		for(var field : testContext.getTestClass().getDeclaredFields()) {
			if(field.getType() == TopologyTestDriver.class) {
				field.setAccessible(true);
				field.set(testContext.getTestInstance(), driver);
			}
		}
	}
	
	@Override
	public void afterTestMethod(TestContext testContext) throws Exception {
		var driver = (TopologyTestDriver) testContext.getAttribute(TOPOLOGY_TEST_DRIVER);
		
		try {
			driver.close();
		}
		catch(Exception e) {}
		
		testContext.removeAttribute(TOPOLOGY_TEST_DRIVER);
	}
	
	private TopologyTestDriver createTestDriver(ApplicationContext applicationContext) {
		var kafkaProperties = applicationContext.getBean(KafkaProperties.class);
		var sslBundles = applicationContext.getBeanProvider(SslBundles.class);
		var topology = applicationContext.getBean(Topology.class);
		
		var properties = new Properties();
		properties.putAll(kafkaProperties.buildStreamsProperties(sslBundles.getIfAvailable()));
		return new TopologyTestDriver(topology, properties, Instant.ofEpochMilli(0));
	}
}
