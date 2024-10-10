package eu.cymo.scenario_5.adapter.kafka.user;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.cymo.scenario_5.adapter.kafka.AvroSerdeFactory;
import eu.cymo.scenario_5.users.TombStone;
import eu.cymo.scenario_5.users.UserCreated;
import eu.cymo.scenario_5.users.UserUpdated;
import eu.cymo.scenario_5.users.UserUpserted;
import eu.cymo.scenario_5.users.UserValidated;

@Component
public class StreamTopology {
	private final AvroSerdeFactory serdeFactory;
	private final String usersTopic;
	private final String userStateTopic;

	public StreamTopology(
			AvroSerdeFactory serdeFactory,
			@Value("${topics.users}")
			String usersTopic,
			@Value("${topics.user-state}")
			String userStateTopic) {
		this.serdeFactory = serdeFactory;
		this.usersTopic = usersTopic;
		this.userStateTopic = userStateTopic;
	}
	
	@Autowired
	public void configure(StreamsBuilder builder) {
		builder.stream(usersTopic,
				Consumed.with(Serdes.String(), serdeFactory.specificAvroValueSerde()))
			.mapValues(v -> v == null ? new TombStone() : v)
			.groupByKey()
			.aggregate(
					this::initialize, 
					this::aggregate, 
					Materialized.<String, UserUpserted, KeyValueStore<Bytes, byte[]>> as("user-aggregate")
						.withKeySerde(Serdes.String())
						.withValueSerde(serdeFactory.specificAvroValueSerde()))
			.toStream()
			.to(userStateTopic,
				Produced.with(Serdes.String(), serdeFactory.specificAvroValueSerde()));
	}
	
	private UserUpserted initialize() {
		return null;
	}
	
	private UserUpserted aggregate(String key, SpecificRecord value, UserUpserted aggregate) {
		if(value instanceof UserCreated created) {
			return fromCreated(created);
		}
		else if(value instanceof UserUpdated updated) {
			return fromUpdated(updated);
		}
		else if(value instanceof UserValidated validated) {
			return fromValidated(aggregate, validated);
		}
		else if(value instanceof TombStone) {
			return null;
		}

		throw new IllegalArgumentException("Unexpected type on topic '%s': '%s'".formatted(usersTopic, value.getSchema().getName()));
	}
	
	private UserUpserted fromCreated(UserCreated created) {
		return UserUpserted.newBuilder()
				.setId(created.getId())
				.setFirstName(created.getFirstName())
				.setLastName(created.getLastName())
				.setEmailAddress(created.getEmailAddress())
				.setValidated(created.getValidated())
				.build();
	}
	
	private UserUpserted fromUpdated(UserUpdated updated) {
		return UserUpserted.newBuilder()
				.setId(updated.getId())
				.setFirstName(updated.getFirstName())
				.setLastName(updated.getLastName())
				.setEmailAddress(updated.getEmailAddress())
				.setValidated(updated.getValidated())
				.build();
	}
	
	private UserUpserted fromValidated(UserUpserted aggregate, UserValidated validated) {
		return UserUpserted.newBuilder()
				.setId(aggregate.getId())
				.setFirstName(aggregate.getFirstName())
				.setLastName(aggregate.getLastName())
				.setEmailAddress(aggregate.getEmailAddress())
				.setValidated(true)
				.build();
		
	}
	
}
