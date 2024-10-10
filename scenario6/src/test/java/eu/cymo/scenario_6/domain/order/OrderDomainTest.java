package eu.cymo.scenario_6.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.context.annotation.FilterType.REGEX;

import java.util.UUID;

import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;

import eu.cymo.scenario_6.kafka_topology.TestTopic;
import eu.cymo.scenario_6.kafka_topology.TopologyTest;
import eu.cymo.scenario_6.users.OrderPlaced;
import eu.cymo.scenario_6.users.OrderPlacedEnriched;
import eu.cymo.scenario_6.users.UserUpserted;

@TopologyTest(
        includeFilters = { 
                @ComponentScan.Filter(type = REGEX, pattern = { "eu.cymo.scenario_6.adapter.kafka.*" })})
public class OrderDomainTest {

	@TestTopic("${topics.users}")
	private TestInputTopic<String, UserUpserted> usersTopic;

	@TestTopic("${topics.orders}")
	private TestInputTopic<String, OrderPlaced> ordersTopic;

	@TestTopic("${topics.orders-enriched}")
	private TestOutputTopic<String, OrderPlacedEnriched> ordersEnrichedTopic;
	
	@Test
	void addsUserInfoToOrder() {
		// given
		var user = UserUpserted.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setFirstName("first-name")
				.setLastName("last-name")
				.setEmailAddress("email-address")
				.setValidated(true)
				.build();
		var order = OrderPlaced.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setUserId(user.getId())
				.setProductId(UUID.randomUUID().toString())
				.build();
		
		// when
		usersTopic.pipeInput(user.getId(), user);
		ordersTopic.pipeInput(order.getId(), order);
		
		// then
		assertThat(ordersEnrichedTopic.readKeyValuesToList())
			.hasSize(1)
			.last()
			.satisfies(
					record -> assertThat(record.key).isEqualTo(order.getId()),
					record -> assertThat(record.value).isEqualTo(OrderPlacedEnriched.newBuilder()
							.setId(order.getId())
							.setUserId(user.getId())
							.setUserEmailAddress("email-address")
							.setProductId(order.getProductId())
							.build()));
	}
	
	@Test
	void forwardsNothingWhenNoJoinDone() {
		// given
		var order = OrderPlaced.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setUserId(UUID.randomUUID().toString())
				.setProductId(UUID.randomUUID().toString())
				.build();
		
		// when
		ordersTopic.pipeInput(order.getId(), order);

		// then
		assertThat(ordersEnrichedTopic.readKeyValuesToList())
			.isEmpty();
	}
	
	@Test
	void forwardsNothingWhenUserIdDoesNotMatch() {
		// given
		var user = UserUpserted.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setFirstName("first-name")
				.setLastName("last-name")
				.setEmailAddress("email-address")
				.setValidated(true)
				.build();
		var order = OrderPlaced.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setUserId(UUID.randomUUID().toString())
				.setProductId(UUID.randomUUID().toString())
				.build();
		
		// when
		usersTopic.pipeInput(user.getId(), user);
		ordersTopic.pipeInput(order.getId(), order);

		// then
		assertThat(ordersEnrichedTopic.readKeyValuesToList())
			.isEmpty();
	}
	
}
