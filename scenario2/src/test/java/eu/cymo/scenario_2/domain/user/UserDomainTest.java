package eu.cymo.scenario_2.domain.user;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.springframework.context.annotation.FilterType.REGEX;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Objects;
import java.util.UUID;

import org.apache.avro.generic.GenericRecord;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.cymo.scenario_2.kafka_container.ConsumerAssert;
import eu.cymo.scenario_2.kafka_container.KafkaContainerTest;
import eu.cymo.scenario_2.kafka_container.TestConsumer;

@KafkaContainerTest(
		includeFilters = @ComponentScan.Filter(type = REGEX, pattern = {
			"eu.cymo.scenario_2.adapter.kafka.*",
			"eu.cymo.scenario_2.adapter.http.*",
			"eu.cymo.scenario_2.adapter.mail.*",
			"eu.cymo.scenario_2.adapter.memory.*"}))
class UserDomainTest {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	@TestConsumer(topic = "${topics.users}")
	private ConsumerAssert<String, GenericRecord> consumer;
	
	@SpyBean
	private UserMailService userMailService;
	
	@Autowired
	private MockMvc mvc;
	
	@Autowired
	private UserRepository userRepository;
	
	@Nested
	class CreateUser {
		
		@Test
		void returnsUserResponseDto() throws Exception {
			// when
			var response = mvc.perform(MockMvcRequestBuilders.post("/users")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "firstName": "Returns",
							  "lastName": "User",
							  "emailAddress": "returns-user@test.com"
							}
							"""))
				.andExpect(status().is2xxSuccessful())
				.andReturn();

			
			// then
			assertThatJson(response.getResponse().getContentAsString())
				.isObject()
				.containsOnlyKeys("id", "firstName", "lastName", "emailAddress", "validated")
				.containsEntry("firstName", "Returns")
				.containsEntry("lastName", "User")
				.containsEntry("emailAddress", "returns-user@test.com")
				.containsEntry("validated", false);
		}
		
		@Test
		void publishesUserCreatedMessage() throws Exception {
			// when
			var response = mvc.perform(MockMvcRequestBuilders.post("/users")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "firstName": "Publishes",
							  "lastName": "User",
							  "emailAddress": "publishes-user@test.com"
							}
							"""))
				.andExpect(status().is2xxSuccessful())
				.andReturn();
			var user = toUserResponse(response);

			// then
			consumer.verify(records -> {
				assertThat(records).anySatisfy(item ->
						assertThat(item)
							.satisfies(
									i -> assertThat(i.key()).isEqualTo(user.id()),
									i -> assertThat(i.value())
											.satisfies(
													r -> assertThat(r.getSchema().getName()).isEqualTo("UserCreated"))
											.satisfies(
													r -> assertThat(r.get("id")).isEqualTo(user.id()),
													r -> assertThat(r.get("firstName")).isEqualTo("Publishes"),
													r -> assertThat(r.get("lastName")).isEqualTo("User"),
													r -> assertThat(r.get("emailAddress")).isEqualTo("publishes-user@test.com"),
													r -> assertThat(r.get("validated")).isEqualTo(false))));
			});
		}

		@Test
		void persistsUserEntity() throws Exception {
			// when
			var response = mvc.perform(MockMvcRequestBuilders.post("/users")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "firstName": "Persists",
							  "lastName": "User",
							  "emailAddress": "persists-user@test.com"
							}
							"""))
				.andExpect(status().is2xxSuccessful())
				.andReturn();
			var user = toUserResponse(response);
			
			// then
			Awaitility.await()
				.untilAsserted(() -> assertThat(userRepository.findById(user.id()))
						.hasValueSatisfying(persisted -> assertThat(persisted)
								.satisfies(
										p -> assertThat(p.id()).isEqualTo(user.id()),
										p -> assertThat(p.firstName()).isEqualTo("Persists"),
										p -> assertThat(p.lastName()).isEqualTo("User"),
										p -> assertThat(p.emailAddress()).isEqualTo("persists-user@test.com"),
										p -> assertThat(p.validated()).isEqualTo(false))));
		}
		
		@Test
		void sendsValidateUserMail() throws Exception {
			// when
			var response = mvc.perform(MockMvcRequestBuilders.post("/users")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "firstName": "Mails",
							  "lastName": "User",
							  "emailAddress": "mails-user@test.com"
							}
							"""))
				.andExpect(status().is2xxSuccessful())
				.andReturn();
			var user = toUserResponse(response);
			
			// then
			Awaitility.await()
				.untilAsserted(() -> verify(userMailService).sendValidateUserMail(argThat(u -> Objects.equals(u.id(), user.id()))));
		}
		
	}
	
	@Nested
	class ValidateUser {
		
		@Nested
		class UserExists {
		
			@Test
			void returnsOk() throws Exception {
				// given
				var user = createUser("""
							{
							  "firstName": "New",
							  "lastName": "User",
							  "emailAddress": "new-user@test.com"
							}
						""");
				
				//then
				mvc.perform(MockMvcRequestBuilders.post("/users/{id}/validate", user.id()))
					.andExpect(status().isOk());
				
			}
			
			@Test
			void publishesUserValidatedMessage() throws Exception {
				// given
				var user = createUser("""
							{
							  "firstName": "New",
							  "lastName": "User",
							  "emailAddress": "new-user@test.com"
							}
						""");
				
				//when
				mvc.perform(MockMvcRequestBuilders.post("/users/{id}/validate", user.id()))
					.andExpect(status().isOk());

				// then
				consumer.verify(records -> {
					assertThat(records).anySatisfy(item ->
							assertThat(item)
								.satisfies(
										i -> assertThat(i.key()).isEqualTo(user.id()),
										i -> assertThat(i.value())
											.satisfies(
													r -> assertThat(r.getSchema().getName()).isEqualTo("UserValidated"))
											.satisfies(
													r -> assertThat(r.get("id")).isEqualTo(user.id()))));
				});
			}
			
			@Test
			void persistUserValidated() throws Exception {
				// given
				var user = createUser("""
							{
							  "firstName": "New",
							  "lastName": "User",
							  "emailAddress": "new-user@test.com"
							}
						""");
				
				//when
				mvc.perform(MockMvcRequestBuilders.post("/users/{id}/validate", user.id()))
					.andExpect(status().isOk());

				// then
				Awaitility.await()
					.untilAsserted(() -> assertThat(userRepository.findById(user.id()))
							.hasValueSatisfying(persisted -> assertThat(persisted)
									.satisfies(
											p -> assertThat(p.id()).isEqualTo(user.id()),
											p -> assertThat(p.firstName()).isEqualTo("New"),
											p -> assertThat(p.lastName()).isEqualTo("User"),
											p -> assertThat(p.emailAddress()).isEqualTo("new-user@test.com"),
											p -> assertThat(p.validated()).isEqualTo(true))));
			}
			
		}
		
		@Nested
		class UserDoesNotExist {

			@Test
			void returnsNotFound() throws Exception {
				// given
				var randomId = UUID.randomUUID();
				
				//then
				mvc.perform(MockMvcRequestBuilders.post("/users/{id}/validate", randomId))
					.andExpect(status().isNotFound());
			}
			
		}
		
	}
	
	private UserResponseDto createUser(String json) throws Exception {
		var response = mvc.perform(MockMvcRequestBuilders.post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().is2xxSuccessful())
			.andReturn();
		
		var user = toUserResponse(response);
		waitUntilUserPersisted(user);
		return user;
	}
	
	private UserResponseDto toUserResponse(MvcResult response) throws Exception {
		var content = response.getResponse().getContentAsString();
		return MAPPER.readValue(content, UserResponseDto.class);
	}
	
	private void waitUntilUserPersisted(UserResponseDto user) {
		Awaitility.await()
			.untilAsserted(() -> assertThat(userRepository.findById(user.id())).isPresent());
	}

}
