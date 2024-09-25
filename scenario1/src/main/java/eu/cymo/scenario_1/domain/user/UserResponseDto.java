package eu.cymo.scenario_1.domain.user;

public record UserResponseDto(
		String id,
		String firstName,
		String lastName,
		String emailAddress,
		boolean validated) {

	public static UserResponseDto fromUser(User user) {
		return new UserResponseDto(
				user.id(),
				user.firstName(),
				user.lastName(),
				user.emailAddress(),
				user.validated());
	}
	
}
