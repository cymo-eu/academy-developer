package eu.cymo.stream;

import java.util.Properties;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamApplication {
	private static final Logger log = LoggerFactory.getLogger(StreamApplication.class);

	public static void main(String[] args) throws Exception {
		var builder = new StreamsBuilder();
		new StreamTopology().configure(builder);
		
		var topology = builder.build();
		
		log.info("Starting topology: \n{}", topology.describe());
		
		try(var stream = new KafkaStreams(topology, kafkaProperties())) {
			stream.start();
			while(true) {
				Thread.sleep(1000);
			}
		}
	}
	
	private static Properties kafkaProperties() throws Exception {
		try(var input = StreamApplication.class.getClassLoader().getResourceAsStream("kafka.properties")) {
			var properties = new Properties();
			properties.load(input);
			return properties;
		}
	}
	
}
