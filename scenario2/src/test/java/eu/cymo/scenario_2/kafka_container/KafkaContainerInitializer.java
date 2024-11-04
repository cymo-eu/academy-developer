package eu.cymo.scenario_2.kafka_container;

import java.util.Map;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class KafkaContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		var container = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.1"));
		container.start();
		
		applicationContext.getEnvironment().getPropertySources().addFirst(new MapPropertySource(
				"test-containers", 
				Map.of(
						"spring.kafka.bootstrap-servers", container.getBootstrapServers(),
						"spring.kafka.properties.security.protocol", "PLAINTEXT")));
	}
	
}
