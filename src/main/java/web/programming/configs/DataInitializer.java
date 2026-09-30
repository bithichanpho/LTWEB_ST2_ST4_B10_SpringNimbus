package web.programming.configs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import web.programming.entity.User;
import web.programming.repository.UserRepository;

/**
 * Khởi tạo dữ liệu mẫu để thuận tiện kiểm tra JWT sau khi chạy ứng dụng.
 *
 * Các tài khoản chỉ được tạo nếu email chưa tồn tại, vì vậy restart ứng dụng sẽ
 * không tạo bản ghi trùng.
 */
@Configuration
public class DataInitializer {

	@Bean
	CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			createUserIfNotExists(userRepository, passwordEncoder, "Nguyen Van A", "test@gmail.com", "123456");

			createUserIfNotExists(userRepository, passwordEncoder, "Demo User", "demo@gmail.com", "123456");
		};
	}

	private void createUserIfNotExists(UserRepository userRepository, PasswordEncoder passwordEncoder, String fullName,
			String email, String rawPassword) {
		if (userRepository.findByEmail(email).isPresent()) {
			return;
		}

		User user = new User();
		user.setFullName(fullName);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(rawPassword));
		user.setImages("/images/avatar.svg");

		userRepository.save(user);
	}
}
