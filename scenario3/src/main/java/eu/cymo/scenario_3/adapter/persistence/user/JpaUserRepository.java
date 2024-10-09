package eu.cymo.scenario_3.adapter.persistence.user;

import java.util.Optional;

import org.springframework.stereotype.Component;

import eu.cymo.scenario_3.domain.user.User;
import eu.cymo.scenario_3.domain.user.UserRepository;

@Component
public class JpaUserRepository implements UserRepository {
	private final UserEntityRepository repo;
	
	public JpaUserRepository(
			UserEntityRepository repo) {
		this.repo = repo;
	}
	
	@Override
	public void save(User user) {
		repo.save(toUserEntity(user));
	}

	@Override
	public Optional<User> findById(String id) {
		return repo.findById(id)
				.map(this::toUser);
	}

	@Override
	public void delete(String id) {
		repo.deleteById(id);
	}
	
	private UserEntity toUserEntity(User user) {
		return new UserEntity(
				user.id(),
				user.firstName(),
				user.lastName(),
				user.emailAddress(),
				user.validated());
	}
	
	private User toUser(UserEntity entity) {
		return new User(
				entity.getId(),
				entity.getFirstName(),
				entity.getLastName(),
				entity.getEmailAddress(),
				entity.isValidated());
	}

}
