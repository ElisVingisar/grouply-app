package ee.grouply.backend.api;

import ee.grouply.backend.domain.User;
import ee.grouply.backend.repo.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) { this.userRepository = userRepository; }

    @GetMapping
    public List<User> list() { return userRepository.findAll(); }
}