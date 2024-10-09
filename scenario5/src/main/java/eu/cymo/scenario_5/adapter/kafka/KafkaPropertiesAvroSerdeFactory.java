package eu.cymo.scenario_5.adapter.kafka;

import java.util.Map;

import org.apache.avro.specific.SpecificRecord;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.stereotype.Component;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

@Component
public class KafkaPropertiesAvroSerdeFactory implements AvroSerdeFactory {
    private final KafkaProperties kafkaProperties;
    private final ObjectProvider<SslBundles> sslBundles;

    public KafkaPropertiesAvroSerdeFactory(
            KafkaProperties kafkaProperties,
            ObjectProvider<SslBundles> sslBundles) {
        this.kafkaProperties = kafkaProperties;
        this.sslBundles = sslBundles;
    }

    @Override
    public GenericAvroSerde genericAvroSerde(boolean isKey) {
        var serde = new GenericAvroSerde();
        serde.configure(properties(), isKey);
        return serde;
    }

    @Override
    public <T extends SpecificRecord> SpecificAvroSerde<T> specificAvroSerde(boolean isKey) {
        var serde = new SpecificAvroSerde<T>();
        serde.configure(properties(), isKey);
        return serde;
    }
    
    private Map<String, ?> properties() {
    	return kafkaProperties.buildStreamsProperties(sslBundles.getIfAvailable());
    }

}
