package eu.cymo.scenario_8.adapter.kafka.user;

import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StreamTopology {
	
	public StreamTopology() {
	}

	@Autowired
	public void configure(StreamsBuilder builder) {
		
	}
}
