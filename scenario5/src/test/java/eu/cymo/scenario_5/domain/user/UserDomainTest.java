package eu.cymo.scenario_5.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.context.annotation.FilterType.REGEX;

import java.util.UUID;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;

import eu.cymo.scenario_5.kafka_topology.TestTopic;
import eu.cymo.scenario_5.kafka_topology.TopologyTest;
import eu.cymo.scenario_5.users.UserCreated;
import eu.cymo.scenario_5.users.UserUpdated;
import eu.cymo.scenario_5.users.UserUpserted;
import eu.cymo.scenario_5.users.UserValidated;

@TopologyTest(
        includeFilters = { 
                @ComponentScan.Filter(type = REGEX, pattern = { "eu.cymo.scenario_5.adapter.kafka.*" })})
public class UserDomainTest {

	@TestTopic("${topics.users}")
	private TestInputTopic<String, SpecificRecord> userTopic;

	@TestTopic("${topics.user-state}")
	private TestOutputTopic<String, SpecificRecord> userStateTopic;
	
	@Test
	void processesUserCreated() {
		// given
		var created = UserCreated.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setFirstName("first-name")
				.setLastName("last-name")
				.setEmailAddress("email-address")
				.setValidated(false)
				.build();
		
		// when
		userTopic.pipeInput(created.getId(), created);
		
		// then
		assertThat(userStateTopic.readKeyValuesToList())
			.hasSize(1)
			.last()
			.satisfies(
					record -> assertThat(record.key).isEqualTo(created.getId()),
					record -> assertThat(record.value).isEqualTo(UserUpserted.newBuilder()
							.setId(created.getId())
							.setFirstName("first-name")
							.setLastName("last-name")
							.setEmailAddress("email-address")
							.setValidated(false)
							.build()));
	}
	
	@Test
	void processesUserValidated() {
		// given
		var created = UserCreated.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setFirstName("first-name")
				.setLastName("last-name")
				.setEmailAddress("email-address")
				.setValidated(false)
				.build();
		var validated = UserValidated.newBuilder()
				.setId(created.getId())
				.build();

		// when
		userTopic.pipeInput(created.getId(), created);
		userTopic.pipeInput(validated.getId(), validated);

		// then
		assertThat(userStateTopic.readKeyValuesToList())
			.hasSize(2)
			.last()
			.satisfies(
					record -> assertThat(record.key).isEqualTo(created.getId()),
					record -> assertThat(record.value).isEqualTo(UserUpserted.newBuilder()
							.setId(created.getId())
							.setFirstName("first-name")
							.setLastName("last-name")
							.setEmailAddress("email-address")
							.setValidated(true)
							.build()));
	}

	@Test
	void processesUserValidtaed() {
		// given
		var created = UserCreated.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setFirstName("first-name")
				.setLastName("last-name")
				.setEmailAddress("email-address")
				.setValidated(false)
				.build();
		var updated = UserUpdated.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setFirstName("first-name-updated")
				.setLastName("last-name")
				.setEmailAddress("email-address")
				.setValidated(false)
				.build();

		// when
		userTopic.pipeInput(created.getId(), created);
		userTopic.pipeInput(updated.getId(), updated);

		// then
		assertThat(userStateTopic.readKeyValuesToList())
			.hasSize(2)
			.last()
			.satisfies(
					record -> assertThat(record.key).isEqualTo(created.getId()),
					record -> assertThat(record.value).isEqualTo(UserUpserted.newBuilder()
							.setId(created.getId())
							.setFirstName("first-name-updated")
							.setLastName("last-name")
							.setEmailAddress("email-address")
							.setValidated(false)
							.build()));
	}
	
	@Test
	void validatedEventUpdatesCorrelatedCreatedEvent() {
		// given
		var created1 = UserCreated.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setFirstName("first-name-1")
				.setLastName("last-name-1")
				.setEmailAddress("email-address-1")
				.setValidated(false)
				.build();
		var created2 = UserCreated.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setFirstName("first-name-2")
				.setLastName("last-name-2")
				.setEmailAddress("email-address-2")
				.setValidated(false)
				.build();
		var validated = UserValidated.newBuilder()
				.setId(created1.getId())
				.build();
		
		// when
		userTopic.pipeInput(created1.getId(), created1);
		userTopic.pipeInput(created2.getId(), created2);
		userTopic.pipeInput(validated.getId(), validated);

		// then
		assertThat(userStateTopic.readKeyValuesToList())
			.hasSize(3)
			.last()
			.satisfies(
					record -> assertThat(record.key).isEqualTo(created1.getId()),
					record -> assertThat(record.value).isEqualTo(UserUpserted.newBuilder()
							.setId(created1.getId())
							.setFirstName("first-name-1")
							.setLastName("last-name-1")
							.setEmailAddress("email-address-1")
							.setValidated(true)
							.build()));
	}
	
}
