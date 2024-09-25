package eu.cymo.scenario_1.kafka_container;

import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin.NewTopics;

@Configuration
@ConfigurationProperties("test")
public class TopicInitializer {
	private List<Topic> topics = new ArrayList<>();

	public List<Topic> getTopics() {
		return topics;
	}

	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}
	
	@Bean
	public NewTopics topics() {
		return new NewTopics(newTopics());
	}
	
	private NewTopic[]  newTopics() {
		return topics.stream()
				.map(this::toNewTopic)
				.toArray(NewTopic[]::new);
	}
	
	private NewTopic toNewTopic(Topic topic) {
		return TopicBuilder.name(topic.name)
				.partitions(topic.partitions != null ? topic.partitions : 1)
				.build();
	}
	
	public record Topic(String name, Integer partitions) {}
	
}
