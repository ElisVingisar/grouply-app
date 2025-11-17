package ee.grouply.backend.config;

import ee.grouply.backend.dto.ExpenseCreateDTO;
import ee.grouply.backend.domain.SplitMode;
import ee.grouply.backend.domain.User;
import ee.grouply.backend.repo.UserRepository;
import ee.grouply.backend.service.ExpenseService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final ExpenseService expenseService;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, ExpenseService expenseService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.expenseService = expenseService;
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