package eu.cymo.scenario_1.adapter.http.user;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.cymo.scenario_1.domain.user.UserRequestDto;
import eu.cymo.scenario_1.domain.user.UserResponseDto;

@RestController
@RequestMapping(path = "/users")
public class UserController {
	
	@PostMapping(
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserResponseDto> createUser(
			@RequestBody UserRequestDto userDto) throws Exception {
		return null;
	}

	@PostMapping(
			path = "/{id}/validate")
	public ResponseEntity<Void> validateUser(
			@PathVariable("id") String id) throws Exception {
		return null;
	}
	
}
