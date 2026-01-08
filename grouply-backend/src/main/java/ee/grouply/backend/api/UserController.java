package ee.grouply.backend.api;

import ee.grouply.backend.entity.User;
import ee.grouply.backend.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<UserDTO> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private UserDTO toDTO(User u) {
        UserDTO dto = new UserDTO();
        dto.id = u.getId();
        dto.name = u.getName();
        dto.email = u.getEmail();
        return dto;
    }

    public static class UserDTO {
        public Long id;
        public String name;
        public String email;
    }
}