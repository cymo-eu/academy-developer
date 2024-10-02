package eu.cymo.stream;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StreamTopologyTest {
	private TopologyTestDriver driver;

	private TestInputTopic<String, String> input;
	private TestOutputTopic<String, String> output;
	
	@BeforeEach
	void setup() {
		var builder = new StreamsBuilder();
		new StreamTopology().configure(builder);
		var topology = builder.build();
		
		driver = new TopologyTestDriver(topology);
		
		input = driver.createInputTopic("input", new StringSerializer(), new StringSerializer());
		output = driver.createOutputTopic("output", new StringDeserializer(), new StringDeserializer());
	}
	
	@AfterEach
	void breakDown() {
		driver.close();
	}

	@Test
	void doSomeTests() {
	}

}
