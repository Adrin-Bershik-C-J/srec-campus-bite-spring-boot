package com.adrin.fc;

import com.adrin.fc.entity.User;
import com.adrin.fc.enums.Role;
import com.adrin.fc.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class FcApplication {

	public static void main(String[] args) {
		SpringApplication.run(FcApplication.class, args);
	}

	@Bean
	CommandLineRunner createDefaultAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			String defaultAdminEmail = "cjabershik@gmail.com";
			if (userRepository.findByEmail(defaultAdminEmail).isEmpty()) {
				User admin = new User();
				admin.setEmail(defaultAdminEmail);
				admin.setName("Super Admin");
				admin.setPassword(passwordEncoder.encode("Admin@123"));
				admin.setRole(Role.ADMIN);
				admin.setVerified(true);
				userRepository.save(admin);
				System.out.println(
						"Default Admin created with email: " + defaultAdminEmail + " and password: Admin@123");
			}
		};
	}
}
