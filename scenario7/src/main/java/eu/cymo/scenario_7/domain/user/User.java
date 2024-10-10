package eu.cymo.scenario_7.domain.user;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public record User(
		String id,
		String firstName,
		String lastName,
		String emailAddress,
		boolean validated) {
	
	public User validate() {
		return new User(
				id,
				firstName,
				lastName,
				emailAddress,
				true);
	}

	@Override
	public final boolean equals(Object obj) {
		if(obj instanceof User other) {
			return new EqualsBuilder()
					.append(id, other.id)
					.build();
		}
		return false;
	}
	
	@Override
	public final int hashCode() {
		return new HashCodeBuilder()
				.append(id)
				.build();
	}
	
}