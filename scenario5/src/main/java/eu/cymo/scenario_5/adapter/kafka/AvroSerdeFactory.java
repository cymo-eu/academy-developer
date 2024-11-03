package eu.cymo.scenario_5.adapter.kafka;

import org.apache.avro.specific.SpecificRecord;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

public interface AvroSerdeFactory {

    default GenericAvroSerde genericAvroValueSerde() {
        return genericAvroSerde(false);
    }

    default <T extends SpecificRecord> SpecificAvroSerde<T> specificAvroValueSerde() {
        return specificAvroSerde(false);
    }
    
    <T extends SpecificRecord> SpecificAvroSerde<T> specificAvroSerde(boolean isKey);
    
    GenericAvroSerde genericAvroSerde(boolean isKey);

}
