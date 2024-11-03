package eu.cymo.scenario_1.domain.user;

import java.io.Serial;

public class PublishUserEventException extends Exception {

	@Serial
	private static final long serialVersionUID = 7026658004298840047L;

	public PublishUserEventException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
}
