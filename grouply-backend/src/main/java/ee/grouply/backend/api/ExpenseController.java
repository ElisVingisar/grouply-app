package ee.grouply.backend.api;

import ee.grouply.backend.dto.ExpenseCreateDTO;
import ee.grouply.backend.dto.ExpenseDTO;
import ee.grouply.backend.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseDTO createExpense(
            @PathVariable Long eventId,
            @RequestBody @Valid ExpenseCreateDTO body,
            @AuthenticationPrincipal UserDetails currentUser) {
        body.eventId = eventId;
        return expenseService.createExpense(body, currentUser.getUsername());
    }

    @GetMapping
    public List<ExpenseDTO> listExpenses(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserDetails currentUser) {
        return expenseService.listByEvent(eventId, currentUser.getUsername());
    }

    @PutMapping("/{expenseId}")
    public ExpenseDTO updateExpense(
            @PathVariable Long eventId,
            @PathVariable Long expenseId,
            @RequestBody @Valid ExpenseCreateDTO body,
            @AuthenticationPrincipal UserDetails currentUser) {
        body.eventId = eventId;
        return expenseService.updateExpense(expenseId, body, currentUser.getUsername());
    }

    @DeleteMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(
            @PathVariable Long eventId,
            @PathVariable Long expenseId,
            @AuthenticationPrincipal UserDetails currentUser) {
        expenseService.deleteExpense(expenseId, eventId, currentUser.getUsername());
    }
}