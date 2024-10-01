package eu.cymo.scenario_3.domain.user;

public interface UserStatePublisher {

	void upserted(User user) throws PublishUserEventException;
	
	void deleted(String id) throws PublishUserEventException;
	
}
