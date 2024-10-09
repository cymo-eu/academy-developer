package eu.cymo.scenario_3.domain.user;

import java.util.Optional;

public interface UserRepository {

	void save(User user);
	
	Optional<User> findById(String id);
	
	void delete(String id);
	
}
