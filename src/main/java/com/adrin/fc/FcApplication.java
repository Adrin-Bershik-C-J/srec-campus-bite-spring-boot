package com.adrin.fc;

import com.adrin.fc.entity.User;
import com.adrin.fc.enums.Role;
import com.adrin.fc.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class FcApplication {

	public static void main(String[] args) {
		SpringApplication.run(FcApplication.class, args);
	}

	@Bean
	CommandLineRunner createDefaultAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			String defaultAdminRoll = "00000000001"; // Choose a fixed roll number
			if (userRepository.findByRollNumber(defaultAdminRoll).isEmpty()) {
				User admin = new User();
				admin.setRollNumber(defaultAdminRoll);
				admin.setName("Super Admin");
				admin.setPassword(passwordEncoder.encode("Admin@123")); // secure password
				admin.setRole(Role.ADMIN);
				userRepository.save(admin);
				System.out.println(
						"Default Admin created with rollNumber: " + defaultAdminRoll + " and password: Admin@123");
			}
		};
	}
}
