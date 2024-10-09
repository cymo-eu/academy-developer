package eu.cymo.scenario_5.kafka_topology;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(SpringExtension.class)
@BootstrapWith(SpringBootTestContextBootstrapper.class)
@TestExecutionListeners(
		listeners = { TopologyTestDriverTestExecutionListener.class, TestTopicTestExecutionListener.class },
		mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
@TypeExcludeFilters(TopologyTypeExcludeFilter.class)
@Import({ 
    TopologyConfiguration.class, MockAvroSerdeFactory.class })
@ActiveProfiles("test")
public @interface TopologyTest {

    ComponentScan.Filter[] includeFilters() default {};
    
    ComponentScan.Filter[] excludeFilters() default {};

}
