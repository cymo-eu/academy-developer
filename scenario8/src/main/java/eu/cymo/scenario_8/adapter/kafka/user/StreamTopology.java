package eu.cymo.scenario_8.adapter.kafka.user;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.cymo.scenario_8.adapter.kafka.AvroSerdeFactory;
import eu.cymo.scenario_8.orders.OrderPlaced;
import eu.cymo.scenario_8.users.UserOrdersAggregate;
import eu.cymo.scenario_8.users.UserOrdersPerDay;

@Component
public class StreamTopology {
	private final AvroSerdeFactory avroSerdeFactory;
	private final String orderTopic;
	private final String ordersPerUserPerDayTopic;
	
	public StreamTopology(
			AvroSerdeFactory avroSerdeFactory,
			@Value("${topics.orders}")
			String orderTopic,
			@Value("${topics.orders-per-user-per-day}")
			String ordersPerUserPerDayTopic) {
		this.avroSerdeFactory = avroSerdeFactory;
		this.orderTopic = orderTopic;
		this.ordersPerUserPerDayTopic = ordersPerUserPerDayTopic;
	}

	@Autowired
	public void configure(StreamsBuilder builder) {
		var orderPlacedSerde = avroSerdeFactory.<OrderPlaced>specificAvroValueSerde();
        var userOrdersAggregateSerde = avroSerdeFactory.<UserOrdersAggregate>specificAvroValueSerde();
        var userOrdersPerDaySerde = avroSerdeFactory.<UserOrdersPerDay>specificAvroValueSerde();
        
        builder.stream(orderTopic,
        		Consumed.with(Serdes.String(), orderPlacedSerde))
			.groupBy((k, v) -> v.getUserId(), Grouped.with(Serdes.String(), orderPlacedSerde))
			.windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofDays(1)).advanceBy(Duration.ofDays(1)))
			.aggregate(
					this::initialize, 
					this::aggregate,
					Materialized.<String, UserOrdersAggregate, WindowStore<Bytes, byte[]>>as("user-orders-per-day")
						.withKeySerde(Serdes.String())
						.withValueSerde(userOrdersAggregateSerde))
			.suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
			.toStream()
			.map((k, v) -> KeyValue.pair(
					k.key(), 
					UserOrdersPerDay.newBuilder()
						.setId(v.getId())
						.setOrderCount(v.getOrderCount())
						.setDate(LocalDate.ofInstant(k.window().startTime(), ZoneOffset.UTC))
						.build()))
			.to(ordersPerUserPerDayTopic,
					Produced.with(Serdes.String(), userOrdersPerDaySerde));
        
	}
	
	private UserOrdersAggregate initialize() {
		return null;
	}
	
	private UserOrdersAggregate aggregate(String key, OrderPlaced order, UserOrdersAggregate aggregate) {
		if(aggregate == null) {
			return UserOrdersAggregate.newBuilder()
					.setId(order.getUserId())
					.setOrderCount(1l)
					.build();
		}
		return UserOrdersAggregate.newBuilder()
				.setId(order.getUserId())
				.setOrderCount(aggregate.getOrderCount() +  1)
				.build();
	}
}
