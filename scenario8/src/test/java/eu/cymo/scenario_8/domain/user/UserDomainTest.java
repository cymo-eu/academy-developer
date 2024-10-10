package eu.cymo.scenario_8.domain.user;

import static org.springframework.context.annotation.FilterType.REGEX;

import org.springframework.context.annotation.ComponentScan;

import eu.cymo.scenario_8.kafka_topology.TopologyTest;

@TopologyTest(
        includeFilters = { 
                @ComponentScan.Filter(type = REGEX, pattern = { "eu.cymo.scenario_8.adapter.kafka.*" })})
public class UserDomainTest {
}
