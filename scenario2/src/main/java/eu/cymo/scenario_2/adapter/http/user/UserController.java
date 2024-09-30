package eu.cymo.scenario_2.adapter.http.user;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.cymo.scenario_2.adapter.kafka.user.KafkaUserPublisher;
import eu.cymo.scenario_2.domain.user.UserRepository;
import eu.cymo.scenario_2.domain.user.UserRequestDto;
import eu.cymo.scenario_2.domain.user.UserResponseDto;

@RestController
@RequestMapping(path = "/users")
public class UserController {
	
	private final KafkaUserPublisher userPublisher;
	private final UserRepository repo;
	
	public UserController(
			KafkaUserPublisher userPublisher,
			UserRepository repo) {
		this.userPublisher = userPublisher;
		this.repo = repo;
	}

	@PostMapping(
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserResponseDto> createUser(
			@RequestBody UserRequestDto userDto) throws Exception {
		var user = userDto.generateUser();
		
		userPublisher.created(user);
		
		return ResponseEntity.ok(UserResponseDto.fromUser(user));
	}

	@PostMapping(
			path = "/{id}/validate")
	public ResponseEntity<Void> validateUser(
			@PathVariable("id") String id) throws Exception {
		var userO = repo.findById(id);
		if(userO.isPresent()) {
			var user = userO.get();

			userPublisher.validated(user);
			
			return ResponseEntity.ok()
					.build();
		}
		return ResponseEntity.notFound()
				.build();
	}
	
}
