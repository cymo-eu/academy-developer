package eu.cymo.scenario_1.kafka_container;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(SpringExtension.class)
@AutoConfigureWebMvc
@AutoConfigureMockMvc
@OverrideAutoConfiguration(enabled = false)
@BootstrapWith(KafkaContainerTestBootStrapper.class)
@ContextConfiguration(initializers =  KafkaContainerInitializer.class)
@TestExecutionListeners(
		listeners = { ProducerTestExecutionListener.class, ConsumerTestExecutionListener.class },
		mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
@TypeExcludeFilters(KafkaContainerTestExcludeFilter.class)
@Import({ TopicInitializer.class, KafkaAutoConfiguration.class })
@ActiveProfiles("test")
public @interface KafkaContainerTest {

    ComponentScan.Filter[] includeFilters() default {};
    
    ComponentScan.Filter[] excludeFilters() default {};

}
