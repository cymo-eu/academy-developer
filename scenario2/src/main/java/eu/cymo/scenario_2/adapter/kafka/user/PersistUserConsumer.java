package eu.cymo.scenario_2.adapter.kafka.user;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.cymo.scenario_2.domain.user.User;
import eu.cymo.scenario_2.domain.user.UserRepository;
import eu.cymo.scenario_2.domain.user.UserValidated;
import eu.cymo.scenario_2.domain.user.Users;
import eu.cymo.scenario_2.utils.Headers;
import eu.cymo.scenario_2.utils.Records;

@Component
public class PersistUserConsumer {	
	private final ObjectMapper MAPPER = new ObjectMapper();
	private final Logger log = LoggerFactory.getLogger(PersistUserConsumer.class);
	
	private final UserRepository userRepository;
	
	public PersistUserConsumer(
			UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@KafkaListener(
			topics = "${topics.users}",
			groupId = "${consumers.user-persistence}",
			containerFactory = "stringContainerFactory")
	public void process(ConsumerRecord<String, String> record) {
		var eventType = Headers.eventType(record.headers());
		
		switch(eventType) {
			case Users.CREATED   -> created(record);
			case Users.VALIDATED -> validated(record);
			default              -> unknownEventType(eventType);
		}
	}
	
	public void created(ConsumerRecord<String, String> record) {
		userRepository.save(toUser(record));
	}
	
	public void validated(ConsumerRecord<String, String> record) {
		var userValidated = toUserValidated(record);
		
		userRepository.findById(userValidated.id())
			.map(User::validate)
			.ifPresentOrElse(
					userRepository::save,
					() -> { throw new RuntimeException("Cannot validate User '%s', User not found".formatted(userValidated.id())); });
	}
	
	private User toUser(ConsumerRecord<String, String> record) {
		try {
			return MAPPER.readValue(record.value(), User.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to create %S from %s".formatted(User.class, Records.readableString(record)));
		}
	}
	
	private UserValidated toUserValidated(ConsumerRecord<String, String> record) {
		try {
			return MAPPER.readValue(record.value(), UserValidated.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to create %s from %s".formatted(UserValidated.class, Records.readableString(record)));
		}
	}
	
	private void unknownEventType(String eventType) {
		log.warn("Encountered unknown eventType during User persistence '{}', skipping event", eventType);
	}

	
}