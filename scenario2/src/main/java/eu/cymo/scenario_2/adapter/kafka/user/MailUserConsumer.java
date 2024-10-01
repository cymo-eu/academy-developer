package eu.cymo.scenario_2.adapter.kafka.user;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import eu.cymo.scenario_2.domain.user.User;
import eu.cymo.scenario_2.domain.user.UserMailService;
import eu.cymo.scenario_2.users.UserCreated;

@Component
public class MailUserConsumer {
	private final UserMailService userMailService;
	
	public MailUserConsumer(
			UserMailService userMailService) {
		this.userMailService = userMailService;
	}

	@KafkaListener(
			topics = "${topics.users}",
			groupId = "${consumers.user-mail}",
			containerFactory = "specificRecordContainerFactory")
	public void process(ConsumerRecord<String, SpecificRecord> record) {
		if(record.value() instanceof UserCreated evt) {
			created(evt);
		}
	}
	
	public void created(UserCreated evt) {
		userMailService.sendValidateUserMail(new User(
				evt.getId(),
				evt.getFirstName(),
				evt.getLastName(),
				evt.getEmailAddress(),
				evt.getValidated()));
	}
	
}
