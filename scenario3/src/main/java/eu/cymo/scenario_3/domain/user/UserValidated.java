package eu.cymo.scenario_3.domain.user;

public record UserValidated(
		String id) {

	public static UserValidated forUser(User user) {
		return new UserValidated(user.id());
	}
	
}
