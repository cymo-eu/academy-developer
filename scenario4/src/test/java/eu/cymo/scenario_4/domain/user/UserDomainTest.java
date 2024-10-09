package eu.cymo.scenario_4.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.context.annotation.FilterType.REGEX;

import java.util.UUID;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;

import eu.cymo.scenario_4.kafka_topology.TestTopic;
import eu.cymo.scenario_4.kafka_topology.TopologyTest;
import eu.cymo.scenario_4.users.UserUpserted;
import eu.cymo.scenario_4.users.ValidatedUser;

@TopologyTest(
        includeFilters = { 
                @ComponentScan.Filter(type = REGEX, pattern = { "eu.cymo.scenario_4.adapter.kafka.*" })})
public class UserDomainTest {

	@TestTopic("${topics.users}")
	private TestInputTopic<String, SpecificRecord> users;
	
	@TestTopic("${topics.validated-users}")
	private TestOutputTopic<String, SpecificRecord> validatedUsers;
	
	@Test
	void filtersOutNonValidatedUser() {
		// given
		var userUpserted = UserUpserted.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setFirstName("first-name")
				.setLastName("last-name")
				.setEmailAddress("email-address")
				.setValidated(false)
				.build();
		
		// when
		users.pipeInput(userUpserted.getId(), userUpserted);
		
		// then
		assertThat(validatedUsers.readKeyValuesToList())
			.isEmpty();
	}
	
	@Test
	void mapsValidatedUserUpsertedToValidatedUser() {
		// given
		var userUpserted = UserUpserted.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setFirstName("first-name")
				.setLastName("last-name")
				.setEmailAddress("email-address")
				.setValidated(true)
				.build();
		
		// when
		users.pipeInput(userUpserted.getId(), userUpserted);
		
		// then
		assertThat(validatedUsers.readKeyValuesToList())
			.hasSize(1)
			.first()
			.satisfies(
					record -> assertThat(record.key).isEqualTo(userUpserted.getId()),
					record -> assertThat(record.value).isEqualTo(ValidatedUser.newBuilder()
							.setId(userUpserted.getId())
							.setFullName("first-name last-name")
							.build()));
	}
}
