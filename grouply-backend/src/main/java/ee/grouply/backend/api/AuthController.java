package ee.grouply.backend.api;

import ee.grouply.backend.dto.AuthRequestDTO;
import ee.grouply.backend.dto.AuthResponseDTO;
import ee.grouply.backend.dto.RegisterDTO;
import ee.grouply.backend.entity.User;
import ee.grouply.backend.repo.UserRepository;
import ee.grouply.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepo;

    public AuthController(AuthService authService, UserRepository userRepo) {
        this.authService = authService;
        this.userRepo = userRepo;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterDTO dto) {
        var res = authService.register(dto);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO req) {
        var res = authService.authenticate(req);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return ResponseEntity.ok().build();
        var u = userRepo.findByEmail(principal.getUsername()).orElse(null);
        if (u == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new SimpleUserDTO(u.getId(), u.getEmail(), u.getName()));
    }

    static record SimpleUserDTO(Long id, String email, String name) {}
}