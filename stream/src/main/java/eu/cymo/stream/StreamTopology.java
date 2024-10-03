package eu.cymo.stream;

import java.util.Arrays;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamTopology {
	private Logger log = LoggerFactory.getLogger(StreamTopology.class);
	
	public void configure(StreamsBuilder builder) {
		var ktable = builder.stream("input", Consumed.with(Serdes.String(), Serdes.String()))
			.flatMap((k, v) -> Arrays.stream(v.split("\\W+"))
					.map(value -> KeyValue.pair(value, value))
					.toList())
			.groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
			.count();
		
		ktable.toStream()
			.peek((k, v) -> log.info("{}: {}", k, v))
			.to("output", Produced.with(Serdes.String(), Serdes.Long()));
	}
	
}
