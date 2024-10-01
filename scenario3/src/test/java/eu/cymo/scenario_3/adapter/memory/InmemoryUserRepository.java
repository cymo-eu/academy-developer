package eu.cymo.scenario_3.adapter.memory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import eu.cymo.scenario_3.domain.user.User;
import eu.cymo.scenario_3.domain.user.UserRepository;

@Component
public class InmemoryUserRepository implements UserRepository {
	private Map<String, User> users = new ConcurrentHashMap<>();
	
	@Override
	public void save(User user) {
		users.put(user.id(), user);
	}

	@Override
	public Optional<User> findById(String id) {
		return Optional.ofNullable(users.get(id));
	}

}
