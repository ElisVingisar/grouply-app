package ee.grouply.backend.api;

import ee.grouply.backend.domain.Payment;
import ee.grouply.backend.domain.User;
import ee.grouply.backend.repo.PaymentRepository;
import ee.grouply.backend.repo.UserRepository;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class PaymentController {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public static class PaymentDTO {
        @NotNull public Long eventId;
        @NotNull public Long fromUserId;
        @NotNull public Long toUserId;
        @NotNull @DecimalMin("0.01") public BigDecimal amount;
    }

    public PaymentController(PaymentRepository paymentRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/payments")
    public Map<String, Object> createPayment(@RequestBody PaymentDTO dto) {
        User from = userRepository.findById(dto.fromUserId).orElseThrow(() -> new IllegalArgumentException("from user not found"));
        User to = userRepository.findById(dto.toUserId).orElseThrow(() -> new IllegalArgumentException("to user not found"));
        Payment p = new Payment();
        p.setEventId(dto.eventId);
        p.setFromUser(from);
        p.setToUser(to);
        p.setAmount(dto.amount.setScale(2, RoundingMode.HALF_UP));        
        p.setSettled(true);
        var saved = paymentRepository.save(p);
        return Map.of("id", saved.getId());
    }

    /**
     * Get payment history for an event
     * GET /api/events/{eventId}/payments
     */
    @GetMapping("/events/{eventId}/payments")
    public List<Map<String, Object>> getPayments(@PathVariable Long eventId) {
        return paymentRepository.findByEventId(eventId).stream()
                .map(p -> Map.<String, Object>of(
                        "id", p.getId(),
                        "fromUserId", p.getFromUser().getId(),
                        "fromUserName", p.getFromUser().getName(),
                        "toUserId", p.getToUser().getId(),
                        "toUserName", p.getToUser().getName(),
                        "amount", p.getAmount(),
                        "createdAt", p.getCreatedAt(),
                        "settled", p.isSettled()
                ))
                .collect(Collectors.toList());
    }
}