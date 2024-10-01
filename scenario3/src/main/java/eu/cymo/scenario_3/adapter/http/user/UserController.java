package eu.cymo.scenario_3.adapter.http.user;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.cymo.scenario_3.adapter.kafka.user.KafkaUserPublisher;
import eu.cymo.scenario_3.domain.user.User;
import eu.cymo.scenario_3.domain.user.UserRepository;
import eu.cymo.scenario_3.domain.user.UserRequestDto;
import eu.cymo.scenario_3.domain.user.UserResponseDto;

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

	@PutMapping(
			path = "/{id}",
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserResponseDto> updateUser(
			@PathVariable("id") String id,
			@RequestBody UserRequestDto userDto) throws Exception {
		var userO = repo.findById(id);
		if(userO.isPresent()) {
			var user = userO.get();
			
			var updatedUser = new User(
					user.id(),
					userDto.firstName(),
					userDto.lastName(),
					userDto.emailAddress(),
					user.validated());
			
			userPublisher.updated(updatedUser);
			
			return ResponseEntity.ok(UserResponseDto.fromUser(updatedUser));
		}
		return ResponseEntity.notFound()
				.build();
	}
	
	@DeleteMapping(
			path = "/{id}")
	public ResponseEntity<Void> deleteUser(
			@PathVariable("id") String id) throws Exception {
		var userO = repo.findById(id);
		if(userO.isPresent()) {
			
			userPublisher.deleted(id);
			
			return ResponseEntity.ok()
					.build();
		}
		return ResponseEntity.notFound()
				.build();
	}
	
}
