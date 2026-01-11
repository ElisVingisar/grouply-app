package ee.grouply.backend.api;

import ee.grouply.backend.dto.ExpenseCreateDTO;
import ee.grouply.backend.dto.ExpenseDTO;
import ee.grouply.backend.entity.Expense;
import ee.grouply.backend.service.ExpenseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events/{eventId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ExpenseDTO createExpense(@PathVariable Long eventId, @RequestBody ExpenseCreateDTO body) {
        body.eventId = eventId;
        Expense created = expenseService.createExpense(body);
        return toDTO(created);
    }

    @GetMapping
    public List<ExpenseDTO> listExpenses(@PathVariable Long eventId) {
        return expenseService.listByEvent(eventId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing expense
     * PUT /api/events/{eventId}/expenses/{expenseId}
     */
    @PutMapping("/{expenseId}")
    public ExpenseDTO updateExpense(
            @PathVariable Long eventId,
            @PathVariable Long expenseId,
            @RequestBody ExpenseCreateDTO body
    ) {
        body.eventId = eventId;
        Expense updated = expenseService.updateExpense(expenseId, body);
        return toDTO(updated);
    }

    /**
     * Delete an expense
     * DELETE /api/events/{eventId}/expenses/{expenseId}
     */
    @DeleteMapping("/{expenseId}")
    public void deleteExpense(@PathVariable Long eventId, @PathVariable Long expenseId) {
        expenseService.deleteExpense(expenseId);
    }

    private ExpenseDTO toDTO(Expense e) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.id = e.getId();
        dto.eventId = e.getEventId();
        dto.payerId = e.getPayer().getId();
        dto.payerName = e.getPayer().getName();
        dto.amount = e.getAmount();
        dto.description = e.getDescription();
        dto.splitMode = e.getSplitMode().name();
        dto.createdAt = e.getCreatedAt();
        dto.shares = (e.getShares() == null ? List.of() :
                e.getShares().stream().map(s -> {
                    ExpenseDTO.ShareView sv = new ExpenseDTO.ShareView();
                    sv.userId = s.getUser().getId();
                    sv.userName = s.getUser().getName();
                    sv.amount = s.getAmount();
                    sv.value = s.getShareValue();
                    return sv;
                }).collect(Collectors.toList()));
        return dto;
    }
}