package eu.cymo.scenario_3.adapter.kafka.user;

import java.util.concurrent.ExecutionException;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import eu.cymo.scenario_3.adapter.kafka.ProducerRecordBuilder;
import eu.cymo.scenario_3.domain.user.PublishUserEventException;
import eu.cymo.scenario_3.domain.user.User;
import eu.cymo.scenario_3.domain.user.UserStatePublisher;
import eu.cymo.scenario_3.users.UserUpserted;

@Component
public class KafkaUserStatePublisher implements UserStatePublisher {
	private final String topic;
	private final KafkaTemplate<String, SpecificRecord> template;
	
	public KafkaUserStatePublisher(
			@Value("${topics.users-state}") String topic,
			KafkaTemplate<String, SpecificRecord> template) {
		this.topic = topic;
		this.template = template;
	}

	@Override
	public void upserted(User user) throws PublishUserEventException {
		send(ProducerRecordBuilder.<String, SpecificRecord>newBuilder()
				.topic(topic)
				.key(user.id())
				.value(UserUpserted.newBuilder()
						.setId(user.id())
						.setFirstName(user.firstName())
						.setLastName(user.lastName())
						.setEmailAddress(user.emailAddress())
						.setValidated(user.validated())
						.build())
				.build());
	}

	@Override
	public void deleted(String id) throws PublishUserEventException {
		send(ProducerRecordBuilder.<String, SpecificRecord>newBuilder()
				.topic(topic)
				.key(id)
				.value(null)
				.build());
	}
	
	private void send(ProducerRecord<String, SpecificRecord> record) throws PublishUserEventException {
		try {
			template.send(record).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new PublishUserEventException("Failed post record to topic", e);
		}
	}

}
