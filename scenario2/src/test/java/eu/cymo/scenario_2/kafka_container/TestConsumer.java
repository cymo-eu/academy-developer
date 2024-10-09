package eu.cymo.scenario_2.kafka_container;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD})
public @interface TestConsumer {

	String groupId() default "";
	
	String topic();
	
	String[] properties() default {};
	
}
