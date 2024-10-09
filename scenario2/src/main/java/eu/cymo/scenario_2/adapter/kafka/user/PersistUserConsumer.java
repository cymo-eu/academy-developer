package eu.cymo.scenario_2.adapter.kafka.user;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import eu.cymo.scenario_2.domain.user.User;
import eu.cymo.scenario_2.domain.user.UserRepository;
import eu.cymo.scenario_2.users.UserCreated;
import eu.cymo.scenario_2.users.UserValidated;

@Component
public class PersistUserConsumer {	
	private final Logger log = LoggerFactory.getLogger(PersistUserConsumer.class);
	
	private final UserRepository userRepository;
	
	public PersistUserConsumer(
			UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@KafkaListener(
			topics = "${topics.users}",
			groupId = "${consumers.user-persistence}",
			containerFactory = "specificRecordContainerFactory")
	public void process(ConsumerRecord<String, SpecificRecord> record) {

		if(record.value() instanceof UserCreated evt) {
			created(evt);
		}
		else if(record.value() instanceof UserValidated evt) {
			validated(evt);
		}
		else {
			unknownEvent(record.value());
		}
		
	}
	
	public void created(UserCreated evt) {
		userRepository.save(new User(
				evt.getId(),
				evt.getFirstName(),
				evt.getLastName(),
				evt.getEmailAddress(),
				evt.getValidated()));
	}
	
	public void validated(UserValidated evt) {
		userRepository.findById(evt.getId())
			.map(User::validate)
			.ifPresentOrElse(
					userRepository::save,
					() -> { throw new RuntimeException("Cannot validate User '%s', User not found".formatted(evt.getId())); });
	}
	
	private void unknownEvent(SpecificRecord evt) {
		log.warn("Encountered unknown schema during User persistence '{}', skipping event", evt.getSchema().getName());
	}

	
}