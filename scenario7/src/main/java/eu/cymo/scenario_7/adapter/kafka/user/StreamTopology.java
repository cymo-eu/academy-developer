package eu.cymo.scenario_7.adapter.kafka.user;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.TimestampedKeyValueStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.cymo.scenario_7.adapter.kafka.AvroSerdeFactory;
import eu.cymo.scenario_7.domain.user.User;
import eu.cymo.scenario_7.domain.user.UserMailService;
import eu.cymo.scenario_7.users.UserUpserted;

@Component
public class StreamTopology {
	private final String NON_VALIDATED_USER_STORE = "non-validated-user-store";
	
	private final AvroSerdeFactory avroSerdeFactory;
	private final UserMailService userMailService;
	private final String userTopic;
	
	public StreamTopology(
			AvroSerdeFactory avroSerdeFactory,
			UserMailService userMailService,
			@Value("${topics.users}")
			String userTopic) {
		this.avroSerdeFactory = avroSerdeFactory;
		this.userMailService = userMailService;
		this.userTopic = userTopic;
	}

	@Autowired
	public void configure(StreamsBuilder builder) {
		var userUpsertedSerde = avroSerdeFactory.<UserUpserted>specificAvroValueSerde();
		
		builder.addStateStore(Stores.timestampedKeyValueStoreBuilder(
				Stores.persistentTimestampedKeyValueStore(NON_VALIDATED_USER_STORE),
				Serdes.String(),
				userUpsertedSerde));
		
		builder.stream(userTopic,
				Consumed.with(Serdes.String(), userUpsertedSerde))
			.process(() -> new UserMailProcessor(userMailService), NON_VALIDATED_USER_STORE);
	}

	private class UserMailProcessor implements Processor<String, UserUpserted, Object, Object> {
		private UserMailService userMailService;
		
		private TimestampedKeyValueStore<String, UserUpserted> usersStore;
		
		public UserMailProcessor(
				UserMailService userMailService) {
			this.userMailService = userMailService;
		}
		
		@Override
		public void init(ProcessorContext<Object, Object> context) {
			usersStore = context.getStateStore(NON_VALIDATED_USER_STORE);
			
			context.schedule(
					Duration.ofMinutes(10), 
					PunctuationType.WALL_CLOCK_TIME,
					this::processNonValidatedUsers);
		}
		
		@Override
		public void process(Record<String, UserUpserted> record) {
			var key = record.key();
			var userUpserted = record.value();
			
			if(userUpserted.getValidated()) {
				usersStore.delete(key);
			}
			else if(usersStore.get(key) == null) {
				usersStore.put(key, ValueAndTimestamp.make(userUpserted, record.timestamp()));
			}
		}
		
		private void processNonValidatedUsers(long timestamp) {
			try(var users = usersStore.all()) {
				while(users.hasNext()) {
					var entry = users.next();
					var key = entry.key;
					var tsAndValue = entry.value;
					var user = tsAndValue.value();
					
					if(isOlderThanAMonth(timestamp, tsAndValue)) {
						userMailService.sendValidateUserMail(
								new User(
										user.getId(),
										user.getFirstName(),
										user.getLastName(),
										user.getEmailAddress(),
										user.getValidated()));
						usersStore.delete(key);
					}
				}
			}
		}
		
		private boolean isOlderThanAMonth(long timestamp, ValueAndTimestamp<UserUpserted> tsAndValue) {
			return ChronoUnit.DAYS.between(
					Instant.ofEpochMilli(tsAndValue.timestamp()),
					Instant.ofEpochMilli(timestamp)) >= 30;
		}
		
	}
}
