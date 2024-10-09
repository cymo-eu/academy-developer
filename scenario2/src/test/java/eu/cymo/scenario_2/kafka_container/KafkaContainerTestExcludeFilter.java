package eu.cymo.scenario_2.kafka_container;

import org.springframework.boot.test.autoconfigure.filter.StandardAnnotationCustomizableTypeExcludeFilter;

public class KafkaContainerTestExcludeFilter extends StandardAnnotationCustomizableTypeExcludeFilter<KafkaContainerTest> {
    
    protected KafkaContainerTestExcludeFilter(Class<KafkaContainerTest> testClass) {
        super(testClass);
    }
    
}
