package eu.cymo.scenario_1.adapter.kafka.user;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.cymo.scenario_1.domain.user.User;
import eu.cymo.scenario_1.domain.user.UserMailService;
import eu.cymo.scenario_1.domain.user.Users;
import eu.cymo.scenario_1.utils.Headers;
import eu.cymo.scenario_1.utils.Records;

@Component
public class MailUserConsumer {
	private final ObjectMapper MAPPER = new ObjectMapper();

	private final UserMailService userMailService;
	
	public MailUserConsumer(
			UserMailService userMailService) {
		this.userMailService = userMailService;
	}

	@KafkaListener(
			topics = "${topics.users}",
			groupId = "${consumers.user-mail}",
			containerFactory = "stringContainerFactory")
	public void process(ConsumerRecord<String, String> record) {
		var eventType = Headers.eventType(record.headers());
		
		switch(eventType) {
			case Users.CREATED   -> created(record);
			default              -> {}
		}
	}
	
	public void created(ConsumerRecord<String, String> record) {
		userMailService.sendValidateUserMail(toUser(record));
	}
	
	private User toUser(ConsumerRecord<String, String> record) {
		try {
			return MAPPER.readValue(record.value(), User.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to create %S from %s".formatted(User.class, Records.readableString(record)));
		}
	}
	
}
