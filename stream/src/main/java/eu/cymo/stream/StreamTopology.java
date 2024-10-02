package eu.cymo.stream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamTopology {
	private Logger log = LoggerFactory.getLogger(StreamTopology.class);
	
	public void configure(StreamsBuilder builder) {
		builder.stream("input", Consumed.with(Serdes.String(), Serdes.String()))
			.foreach((k, v) -> {
				log.info("{}: {}", k, v);
			});
	}
	
}
