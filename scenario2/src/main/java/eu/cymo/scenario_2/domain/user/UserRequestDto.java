package eu.cymo.scenario_2.domain.user;

import java.util.UUID;

public record UserRequestDto(
		String firstName,
		String lastName,
		String emailAddress) {

	public User generateUser() {
		return generateUser(UUID.randomUUID().toString());
	}

	public User generateUser(String id) {
		return new User(
				id,
				firstName,
				lastName,
				emailAddress,
				false);
	}
	
}
