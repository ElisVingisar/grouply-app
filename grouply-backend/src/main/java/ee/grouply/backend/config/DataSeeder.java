package ee.grouply.backend.config;

import ee.grouply.backend.entity.User;
import ee.grouply.backend.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;


@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // WARNING: Demo-only seeding. Remove or guard with a profile check for production.

        if (!userRepository.existsByEmail("admin@localhost")) {
            User admin = new User();
            admin.setEmail("admin@localhost");
            admin.setName("Admin");
            admin.setPasswordHash(passwordEncoder.encode("pass"));
            userRepository.save(admin);
        }
    }
}