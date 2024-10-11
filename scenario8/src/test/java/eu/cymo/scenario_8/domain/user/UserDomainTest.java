package eu.cymo.scenario_8.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.context.annotation.FilterType.REGEX;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;

import eu.cymo.scenario_8.kafka_topology.TestTopic;
import eu.cymo.scenario_8.kafka_topology.TopologyTest;
import eu.cymo.scenario_8.orders.OrderPlaced;
import eu.cymo.scenario_8.users.UserOrdersPerDay;

@TopologyTest(
        includeFilters = { 
                @ComponentScan.Filter(type = REGEX, pattern = { "eu.cymo.scenario_8.adapter.kafka.*" })})
public class UserDomainTest {

	@TestTopic("${topics.orders}")
	private TestInputTopic<String, OrderPlaced> orderTopic;

	@TestTopic("${topics.orders-per-user-per-day}")
	private TestOutputTopic<String, UserOrdersPerDay> ordersPerUserPerDayTopic;
	
	@Test
	void countsSingleEntryForUser() {
		// given
		pipeOrderPlaced(orderPlaced("user-id"), 0);
		
		// when
		progressStreamTimeForDays(1);
		
		// then
		var result = ordersPerUserPerDayTopic.readKeyValuesToList();
		
		assertThat(result)
			.filteredOn(kv -> Objects.equals(kv.key, "user-id"))
			.hasSize(1)
			.last()
			.satisfies(record -> assertThat(record.value).isEqualTo(UserOrdersPerDay.newBuilder()
					.setId("user-id")
					.setDate(LocalDate.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC))
					.setOrderCount(1l)
					.build()));
	}

	@Test
	void countsMultipleEntriesForUser() {
		// given
		pipeOrderPlaced(orderPlaced("user-id"), 0);
		pipeOrderPlaced(orderPlaced("user-id"), 0);
		
		// when
		progressStreamTimeForDays(1);
		
		// then
		var result = ordersPerUserPerDayTopic.readKeyValuesToList();
		
		assertThat(result)
			.filteredOn(kv -> Objects.equals(kv.key, "user-id"))
			.hasSize(1)
			.last()
			.satisfies(record -> assertThat(record.value).isEqualTo(UserOrdersPerDay.newBuilder()
					.setId("user-id")
					.setDate(LocalDate.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC))
					.setOrderCount(2l)
					.build()));
	}

	@Test
	void countsEntriesForUserOverMultipleDays() {
		// when
		pipeOrderPlaced(orderPlaced("user-id"), 0);
		pipeOrderPlaced(orderPlaced("user-id"), 0);

		progressStreamTimeForDays(1);

		pipeOrderPlaced(orderPlaced("user-id"), 1);
		pipeOrderPlaced(orderPlaced("user-id"), 1);
		pipeOrderPlaced(orderPlaced("user-id"), 1);

		progressStreamTimeForDays(2);
		
		// then
		var result = ordersPerUserPerDayTopic.readKeyValuesToList();
		
		assertThat(result)
			.filteredOn(kv -> Objects.equals(kv.key, "user-id"))
			.hasSize(2)
			.satisfiesExactly(
					record -> assertThat(record.value).isEqualTo(UserOrdersPerDay.newBuilder()
						.setId("user-id")
						.setDate(LocalDate.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC))
						.setOrderCount(2l)
						.build()),
					record -> assertThat(record.value).isEqualTo(UserOrdersPerDay.newBuilder()
						.setId("user-id")
						.setDate(LocalDate.ofInstant(Instant.ofEpochMilli(Duration.ofDays(1).toMillis()), ZoneOffset.UTC))
						.setOrderCount(3l)
						.build()));
	}

	@Test
	void countsEntriesForMultipleUsersForTheSameDay() {
		// given
		pipeOrderPlaced(orderPlaced("user-1"), 0);
		pipeOrderPlaced(orderPlaced("user-1"), 0);
		pipeOrderPlaced(orderPlaced("user-2"), 0);

		// when
		progressStreamTimeForDays(1);
		
		// then
		var result = ordersPerUserPerDayTopic.readKeyValuesToList();

		assertThat(result)
			.filteredOn(kv -> Objects.equals(kv.key, "user-1"))
			.hasSize(1)
			.last()
			.satisfies(
					record -> assertThat(record.value).isEqualTo(UserOrdersPerDay.newBuilder()
						.setId("user-1")
						.setDate(LocalDate.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC))
						.setOrderCount(2l)
						.build()));
		
		assertThat(result)
			.filteredOn(kv -> Objects.equals(kv.key, "user-2"))
			.hasSize(1)
			.last()
			.satisfies(
					record -> assertThat(record.value).isEqualTo(UserOrdersPerDay.newBuilder()
						.setId("user-2")
						.setDate(LocalDate.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC))
						.setOrderCount(1l)
						.build()));
	}

	@Test
	void countsEntriesForMultipleUsersForTheMultipleDays() {
		// when
		pipeOrderPlaced(orderPlaced("user-1"), 0);
		pipeOrderPlaced(orderPlaced("user-1"), 0);
		pipeOrderPlaced(orderPlaced("user-2"), 0);

		progressStreamTimeForDays(1);
		
		pipeOrderPlaced(orderPlaced("user-1"), 1);
		pipeOrderPlaced(orderPlaced("user-2"), 1);
		pipeOrderPlaced(orderPlaced("user-2"), 1);

		progressStreamTimeForDays(2);
		
		// then
		var result = ordersPerUserPerDayTopic.readKeyValuesToList();

		assertThat(result)
			.filteredOn(kv -> Objects.equals(kv.key, "user-1"))
			.hasSize(2)
			.satisfiesExactly(
					record -> assertThat(record.value).isEqualTo(UserOrdersPerDay.newBuilder()
							.setId("user-1")
							.setDate(LocalDate.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC))
							.setOrderCount(2l)
							.build()),
					record -> assertThat(record.value).isEqualTo(UserOrdersPerDay.newBuilder()
							.setId("user-1")
							.setDate(LocalDate.ofInstant(Instant.ofEpochMilli(Duration.ofDays(1).toMillis()), ZoneOffset.UTC))
							.setOrderCount(1l)
							.build()));
		
		assertThat(result)
			.filteredOn(kv -> Objects.equals(kv.key, "user-2"))
			.hasSize(2)
			.satisfiesExactly(
					record -> assertThat(record.value).isEqualTo(UserOrdersPerDay.newBuilder()
						.setId("user-2")
						.setDate(LocalDate.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC))
						.setOrderCount(1l)
						.build()),
				record -> assertThat(record.value).isEqualTo(UserOrdersPerDay.newBuilder()
						.setId("user-2")
						.setDate(LocalDate.ofInstant(Instant.ofEpochMilli(Duration.ofDays(1).toMillis()), ZoneOffset.UTC))
						.setOrderCount(2l)
						.build()));
		
	}
	
	private void pipeOrderPlaced(OrderPlaced orderPlaced, int day) {
		orderTopic.pipeInput(
				orderPlaced.getId(),
				orderPlaced,
				Instant.ofEpochMilli(Duration.ofDays(day).toMillis()));
	}
	
	private OrderPlaced orderPlaced(String userId) {
		return OrderPlaced.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setUserId(userId)
				.setProductId(UUID.randomUUID().toString())
				.build();
	}
	
	/**
	 * Windowing is triggered by stream time, so we need to
	 * forward a message with a specified timestamp to ensure
	 * that windows move forward and end up being closed
	 */
	private void progressStreamTimeForDays(int days) {
		var orderPlaced = orderPlaced("stream-time-progressing-user");
		orderTopic.pipeInput(
				orderPlaced.getId(),
				orderPlaced, Instant.ofEpochMilli(Duration.ofDays(days).toMillis()));
	}
}
