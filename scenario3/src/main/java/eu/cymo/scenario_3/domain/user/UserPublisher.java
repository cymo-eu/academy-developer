package eu.cymo.scenario_3.domain.user;

public interface UserPublisher {

	void created(User user) throws PublishUserEventException;

	void validated(User user) throws PublishUserEventException;
	
	void updated(User user) throws PublishUserEventException;
	
	void deleted(String userId) throws PublishUserEventException;

}
