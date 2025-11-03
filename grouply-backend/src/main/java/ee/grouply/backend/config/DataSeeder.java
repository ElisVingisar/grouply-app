package ee.grouply.backend.config;

import ee.grouply.backend.dto.ExpenseCreateDTO;
import ee.grouply.backend.dto.ShareDTO;
import ee.grouply.backend.domain.SplitMode;
import ee.grouply.backend.domain.User;
import ee.grouply.backend.repo.UserRepository;
import ee.grouply.backend.service.ExpenseService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final ExpenseService expenseService;

    public DataSeeder(UserRepository userRepository, ExpenseService expenseService) {
        this.userRepository = userRepository;
        this.expenseService = expenseService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create test users if none exist
        if (userRepository.count() == 0) {
            var users = List.of(
                createUser("Alice", "alice@example.com"),
                createUser("Bob", "bob@example.com"),
                createUser("Charlie", "charlie@example.com"),
                createUser("David", "david@example.com")
            );
            userRepository.saveAll(users);
            System.out.println("Seeded users: Alice, Bob, Charlie, David");
        }
    }

    private User createUser(String name, String email) {
        var user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}