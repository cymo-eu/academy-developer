package eu.cymo.scenario_4.adapter.kafka;

import java.util.HashMap;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

@EnableKafkaStreams
@Configuration
public class KafkaConfiguration {

    @Bean(KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig(
            KafkaProperties kafkaProperties,
            ObjectProvider<SslBundles> sslBundles) {
        var properties = new HashMap<String, Object>();
        properties.putAll(kafkaProperties.buildStreamsProperties(sslBundles.getIfAvailable()));
        return new KafkaStreamsConfiguration(properties);
    }

}
