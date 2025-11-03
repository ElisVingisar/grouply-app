package ee.grouply.backend.api;

import ee.grouply.backend.dto.*;
import ee.grouply.backend.domain.Expense;
import ee.grouply.backend.service.ExpenseService;
import ee.grouply.backend.repo.UserRepository;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService, UserRepository userRepository) {
        this.expenseService = expenseService;
    }

    @PostMapping("/expenses")
    public Map<String, Object> createExpense(@Valid @RequestBody ExpenseCreateDTO dto) {
        Expense e = expenseService.createExpense(dto);
        return Map.of("id", e.getId());
    }

    @GetMapping("/events/{id}/expenses")
    public List<ExpenseDTO> listByEvent(@PathVariable("id") Long eventId) {
        var expenses = expenseService.listByEvent(eventId);
        return expenses.stream().map(e -> {
            var x = new ExpenseDTO();
            x.id = e.getId();
            x.eventId = e.getEventId();
            x.payerId = e.getPayer().getId();
            x.payerName = e.getPayer().getName();
            x.amount = e.getAmount();
            x.description = e.getDescription();
            x.splitMode = e.getSplitMode().name();
            x.createdAt = e.getCreatedAt();
            x.shares = e.getShares().stream().map(s -> {
                var sv = new ExpenseDTO.ShareView();
                sv.userId = s.getUser().getId();
                sv.userName = s.getUser().getName();
                sv.amount = s.getAmount();
                return sv;
            }).collect(Collectors.toList());
            return x;
        }).collect(Collectors.toList());
    }
}