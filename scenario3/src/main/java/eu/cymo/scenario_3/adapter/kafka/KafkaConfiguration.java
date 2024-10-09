package eu.cymo.scenario_3.adapter.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;

@EnableKafka
@Configuration
public class KafkaConfiguration {

	@Bean
	@Primary
	public KafkaTemplate<?, ?> primaryKafkaTemplate(KafkaTemplate<?, ?> template) {
		template.setAllowNonTransactional(true);
		return template;
	}
	
}
