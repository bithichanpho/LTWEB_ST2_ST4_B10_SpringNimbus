package web.programming.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import web.programming.entity.User;
import web.programming.models.LoginUserModel;
import web.programming.models.RegisterUserModel;
import web.programming.repository.UserRepository;

@Service
public class AuthenticationService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final AuthenticationManager authenticationManager;

	public AuthenticationService(
			UserRepository userRepository,
			AuthenticationManager authenticationManager,
			PasswordEncoder passwordEncoder
	) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public User authenticate(LoginUserModel input) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						input.getEmail(),
						input.getPassword()
				)
		);

		return userRepository.findByEmail(input.getEmail())
				.orElseThrow();
	}

	public User signup(RegisterUserModel input) {
		User user = new User();
		user.setFullName(input.getFullName());
		user.setEmail(input.getEmail());
		user.setPassword(passwordEncoder.encode(input.getPassword()));
		// Cột images là NOT NULL; slide gốc không set khi signup nên cần giá trị mặc định để đăng ký chạy được
		user.setImages("/images/avatar.svg");

		return userRepository.save(user);
	}

}
