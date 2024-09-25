package eu.cymo.scenario_1.adapter.mail.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.cymo.scenario_1.domain.user.User;
import eu.cymo.scenario_1.domain.user.UserMailService;

@Component
public class LogUserMailService implements UserMailService {

	private static final Logger log = LoggerFactory.getLogger(LogUserMailService.class);
	
	@Override
	public void sendValidateUserMail(User user) {
		log.info("Sending validate user mail for '{}'", user.id());
	}

}
