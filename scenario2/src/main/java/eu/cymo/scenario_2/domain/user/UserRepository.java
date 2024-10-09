package eu.cymo.scenario_2.domain.user;

import java.util.Optional;

public interface UserRepository {

	void save(User user);
	
	Optional<User> findById(String id);
	
}