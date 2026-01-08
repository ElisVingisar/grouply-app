package ee.grouply.backend.service;

import ee.grouply.backend.dto.AuthRequestDTO;
import ee.grouply.backend.dto.AuthResponseDTO;
import ee.grouply.backend.dto.RegisterDTO;
import ee.grouply.backend.entity.User;
import ee.grouply.backend.repo.UserRepository;
import ee.grouply.backend.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponseDTO authenticate(AuthRequestDTO req) {
        var maybe = userRepo.findByEmail(req.getEmail());
        if (maybe.isEmpty()) throw new IllegalArgumentException("Invalid credentials");
        User u = maybe.get();
        String hash = u.getPasswordHash();
        if (hash == null || !passwordEncoder.matches(req.getPassword(), hash)) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(u.getEmail(), u.getId());
        return new AuthResponseDTO(token, u.getId(), u.getEmail(), u.getName());
    }

    public AuthResponseDTO register(RegisterDTO dto) {
        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        User u = new User();
        u.setName(dto.getName());
        u.setEmail(dto.getEmail());
        u.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        u = userRepo.save(u);
        String token = jwtUtil.generateToken(u.getEmail(), u.getId());
        return new AuthResponseDTO(token, u.getId(), u.getEmail(), u.getName());
    }
}