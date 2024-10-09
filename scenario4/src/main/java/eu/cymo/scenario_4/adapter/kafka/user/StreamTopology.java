package eu.cymo.scenario_4.adapter.kafka.user;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.cymo.scenario_4.adapter.kafka.AvroSerdeFactory;
import eu.cymo.scenario_4.users.UserUpserted;
import eu.cymo.scenario_4.users.ValidatedUser;

@Component
public class StreamTopology {
	private final AvroSerdeFactory serdeFactory;
	private final String usersTopic;
	private final String validatedUsersTopic;
	
	public StreamTopology(
			AvroSerdeFactory serdeFactory,
			@Value("${topics.users}")
			String usersTopic,
			@Value("${topics.validated-users}")
			String validatedUsersTopic) {
		this.serdeFactory = serdeFactory;
		this.usersTopic = usersTopic;
		this.validatedUsersTopic = validatedUsersTopic;
	}

	@Autowired
	public void configure(StreamsBuilder builder) {
		builder.stream(usersTopic,
				Consumed.with(Serdes.String(), serdeFactory.<UserUpserted>specificAvroValueSerde()))
			.filter((k, v) -> v.getValidated())
			.mapValues(v -> ValidatedUser.newBuilder()
					.setId(v.getId())
					.setFullName(v.getFirstName() + " " + v.getLastName())
					.build())
			.to(validatedUsersTopic,
				Produced.with(Serdes.String(), serdeFactory.specificAvroValueSerde()));
	}
	
}
