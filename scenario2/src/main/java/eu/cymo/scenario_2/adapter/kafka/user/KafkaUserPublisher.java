package eu.cymo.scenario_2.adapter.kafka.user;

import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.cymo.scenario_2.adapter.kafka.ProducerRecordBuilder;
import eu.cymo.scenario_2.domain.user.PublishUserEventException;
import eu.cymo.scenario_2.domain.user.User;
import eu.cymo.scenario_2.domain.user.UserPublisher;
import eu.cymo.scenario_2.domain.user.UserValidated;
import eu.cymo.scenario_2.domain.user.Users;

@Component
public class KafkaUserPublisher implements UserPublisher {
	
	private final static ObjectMapper MAPPER = new ObjectMapper();
	
	private final String topic;
	private final KafkaTemplate<String, String> template;
	
	public KafkaUserPublisher(
			@Value("${topics.users}") String topic,
			KafkaTemplate<String, String> template) {
		this.topic = topic;
		this.template = template;
	}

	@Override
	public void created(User user) throws PublishUserEventException {
		send(ProducerRecordBuilder.<String, String>newBuilder()
				.topic(topic)
				.key(user.id())
				.value(toJson(user))
				.eventType(Users.CREATED)
				.build());
	}

	@Override
	public void validated(User user) throws PublishUserEventException {
		send(ProducerRecordBuilder.<String, String>newBuilder()
					.topic(topic)
					.key(user.id())
					.value(toJson(UserValidated.forUser(user)))
					.eventType(Users.VALIDATED)
					.build());
	}
	
	private void send(ProducerRecord<String, String> record) throws PublishUserEventException {
		try {
			template.send(record).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new PublishUserEventException("Failed post record to topic", e);
		}
	}

	private String toJson(Object obj) throws PublishUserEventException {
		try {
			return MAPPER.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new PublishUserEventException("Failed to convert User to json", e);
		}
	}
	

}
