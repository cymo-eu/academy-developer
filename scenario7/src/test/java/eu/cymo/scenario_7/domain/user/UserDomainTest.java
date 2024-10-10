package eu.cymo.scenario_7.domain.user;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.context.annotation.FilterType.REGEX;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;

import eu.cymo.scenario_7.kafka_topology.TestTopic;
import eu.cymo.scenario_7.kafka_topology.TopologyTest;
import eu.cymo.scenario_7.users.UserUpserted;

@TopologyTest(
        includeFilters = { 
                @ComponentScan.Filter(type = REGEX, pattern = { "eu.cymo.scenario_7.adapter.kafka.*" })})
public class UserDomainTest {
	
	@MockBean
	private UserMailService userMailService;
	
	@TestTopic("${topics.users}")
	private TestInputTopic<String, UserUpserted> usersTopic;
	
	private TopologyTestDriver driver;
	
	@Test
	void sendsValidationMail_whenUserNotValidatedForAMonth() {
		// given
		var user = UserUpserted.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setFirstName("first-name")
				.setLastName("last-name")
				.setEmailAddress("email-address")
				.setValidated(false)
				.build();
		usersTopic.pipeInput(user.getId(), user);
		
		// when
		driver.advanceWallClockTime(Duration.ofDays(32));
		
		// then
		verify(userMailService).sendValidateUserMail(argThat(u -> Objects.equals(u.id(), user.getId())));
	}
	
	@Test
	void resendsValidationMailOnce() {
		// given
		var user = UserUpserted.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setFirstName("first-name")
				.setLastName("last-name")
				.setEmailAddress("email-address")
				.setValidated(false)
				.build();
		usersTopic.pipeInput(user.getId(), user);
		
		// when
		driver.advanceWallClockTime(Duration.ofDays(32));
		driver.advanceWallClockTime(Duration.ofDays(32));
		driver.advanceWallClockTime(Duration.ofDays(32));
		
		// then
		verify(userMailService, times(1)).sendValidateUserMail(argThat(u -> Objects.equals(u.id(), user.getId())));
	}
	
	@Test
	void doesntSendMail_whenUserBecomesValidated() {
		// given
		var user = UserUpserted.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setFirstName("first-name")
				.setLastName("last-name")
				.setEmailAddress("email-address")
				.setValidated(false)
				.build();
		var userValidated = UserUpserted.newBuilder()
				.setId(user.getId())
				.setFirstName("first-name")
				.setLastName("last-name")
				.setEmailAddress("email-address")
				.setValidated(true)
				.build();
		usersTopic.pipeInput(user.getId(), user);
		usersTopic.pipeInput(user.getId(), userValidated);
		
		// when
		driver.advanceWallClockTime(Duration.ofDays(32));
		
		// then
		verify(userMailService, never()).sendValidateUserMail(argThat(u -> Objects.equals(u.id(), user.getId())));
	}
	
}
