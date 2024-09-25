package eu.cymo.scenario_1.domain.user;

public interface UserPublisher {

	void created(User user) throws PublishUserEventException;

	void validated(User user) throws PublishUserEventException;

}
