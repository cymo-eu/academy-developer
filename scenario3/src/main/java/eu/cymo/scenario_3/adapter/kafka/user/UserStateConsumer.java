package eu.cymo.scenario_3.adapter.kafka.user;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import eu.cymo.kafka_streams.demo.UserCreated;
import eu.cymo.kafka_streams.demo.UserValidated;
import eu.cymo.scenario_3.domain.user.PublishUserEventException;
import eu.cymo.scenario_3.domain.user.User;
import eu.cymo.scenario_3.domain.user.UserRepository;
import eu.cymo.scenario_3.domain.user.UserStatePublisher;
import eu.cymo.scenario_3.users.UserUpdated;

@Component
public class UserStateConsumer {
	private final UserRepository userRepository;
	private final UserStatePublisher userStatePublisher;
	
	public UserStateConsumer(
			UserRepository userRepository,
			UserStatePublisher userStatePublisher) {
		this.userRepository = userRepository;
		this.userStatePublisher = userStatePublisher;
	}
	
	@Transactional("kafkaTransactionManager")
	@KafkaListener(
			topics = "${topics.users}",
			groupId = "${consumers.user-state}",
			containerFactory = "specificRecordContainerFactory")
	public void process(ConsumerRecord<String, SpecificRecord> record) throws PublishUserEventException {
		var key = record.key();
		var value = record.value();
		
		if(value instanceof UserCreated evt) {
			created(evt);
		}
		else if(value instanceof UserValidated evt) {
			validated(evt);
		}
		else if(value instanceof UserUpdated evt) {
			updated(evt);
		}
		else if(value == null) {
			deleted(key);
		}
	}
	
	private void created(UserCreated evt) throws PublishUserEventException {
		var user = new User(
				evt.getId(),
				evt.getFirstName(),
				evt.getLastName(),
				evt.getEmailAddress(),
				evt.getValidated());
		
		userRepository.save(user);
		userStatePublisher.upserted(user);
	}
	
	private void validated(UserValidated evt) throws PublishUserEventException {
		var userO = userRepository.findById(evt.getId());
		if(userO.isPresent()) {
			var updated = userO.map(User::validate)
					.get();

			userRepository.save(updated);
			userStatePublisher.upserted(updated);
		}
		throw new RuntimeException("No user found for id '%s'".formatted(evt.getId()));
	}
	
	private void updated(UserUpdated evt) throws PublishUserEventException {
		var user = new User(
				evt.getId(),
				evt.getFirstName(),
				evt.getLastName(),
				evt.getEmailAddress(),
				evt.getValidated());
		
		userRepository.save(user);
		userStatePublisher.upserted(user);
	}
	
	private void deleted(String id) {
		
	}
}
