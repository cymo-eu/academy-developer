package eu.cymo.scenario_5.kafka_topology;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.KafkaStreamsInfrastructureCustomizer;

@Configuration
@EnableConfigurationProperties({ KafkaProperties.class })
public class TopologyConfiguration {
    
    @Bean
    StreamsBuilder streamsBuilder(
            ObjectProvider<SslBundles> sslBundles,
            KafkaProperties kafkaProperties) {
        var streamsProperties = kafkaProperties.buildStreamsProperties(sslBundles.getIfAvailable());
        var streamsConfig = new StreamsConfig(streamsProperties);
        var topologyConfig = new TopologyConfig(streamsConfig);
        return new StreamsBuilder(topologyConfig);
    }
    
    @Bean
    Topology topology(
    		StreamsBuilder streamsBuilder,
            ObjectProvider<KafkaStreamsInfrastructureCustomizer> infrastructureCustomizer) {
		infrastructureCustomizer.forEach(cust -> cust.configureBuilder(streamsBuilder));
		
		var topology = streamsBuilder.build();
		
		infrastructureCustomizer.forEach(cust -> cust.configureTopology(topology));
		
		return topology;
    }
    
}
