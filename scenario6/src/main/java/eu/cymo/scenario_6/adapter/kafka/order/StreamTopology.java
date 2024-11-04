package eu.cymo.scenario_6.adapter.kafka.order;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.cymo.scenario_6.adapter.kafka.AvroSerdeFactory;
import eu.cymo.scenario_6.orders.OrderPlaced;
import eu.cymo.scenario_6.orders.OrderPlacedEnriched;
import eu.cymo.scenario_6.users.UserUpserted;

@Component
public class StreamTopology {
	private final AvroSerdeFactory avroSerdeFactory;
	private final String usersTopic;
	private final String ordersTopic;
	private final String ordersEnrichedTopic;
	
	public StreamTopology(
			AvroSerdeFactory avroSerdeFactory,
			@Value("${topics.users}")
			String usersTopic,
			@Value("${topics.orders}")
			String ordersTopic,
			@Value("${topics.orders-enriched}")
			String ordersEnrichedTopic) {
		this.avroSerdeFactory = avroSerdeFactory;
		this.usersTopic = usersTopic;
		this.ordersTopic = ordersTopic;
		this.ordersEnrichedTopic = ordersEnrichedTopic;
	}

	@Autowired
	public void configure(StreamsBuilder builder) {
		var userUpsertedSerde = avroSerdeFactory.<UserUpserted>specificAvroValueSerde();
		var orderPlacedSerde = avroSerdeFactory.<OrderPlaced>specificAvroValueSerde();
		var orderPlacedEnrichedSerde = avroSerdeFactory.<OrderPlacedEnriched>specificAvroValueSerde();
		
		var usersTable = builder.table(usersTopic, 
				Consumed.with(Serdes.String(), userUpsertedSerde),
				Materialized.<String, UserUpserted, KeyValueStore<Bytes,byte[]>>as("users-store")
					.withKeySerde(Serdes.String())
					.withValueSerde(userUpsertedSerde));
		
		builder.stream(ordersTopic,
				Consumed.with(Serdes.String(), orderPlacedSerde))
			.selectKey((k,v) -> v.getUserId())
			.join(usersTable, 
					(order, user) -> OrderPlacedEnriched.newBuilder()
						.setId(order.getId())
						.setUserId(order.getUserId())
						.setUserEmailAddress(user.getEmailAddress())
						.setProductId(order.getProductId())
						.build(),
					Joined.<String, OrderPlaced, UserUpserted>as("orders-user-join")
						.withKeySerde(Serdes.String())
						.withValueSerde(orderPlacedSerde)
						.withOtherValueSerde(userUpsertedSerde))
			.selectKey((k, v) -> v.getId())
			.to(ordersEnrichedTopic,
					Produced.with(Serdes.String(), orderPlacedEnrichedSerde));
				
	}
	
}
