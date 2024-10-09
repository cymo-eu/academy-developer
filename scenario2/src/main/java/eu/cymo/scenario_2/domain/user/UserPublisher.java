package eu.cymo.scenario_2.domain.user;

public interface UserPublisher {

	void created(User user) throws PublishUserEventException;

	void validated(User user) throws PublishUserEventException;

}
