package ee.grouply.backend.service;

import ee.grouply.backend.dto.BalanceDTO;
import ee.grouply.backend.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class SettlementService {
    private final ExpenseService expenseService;
    private final UserRepository userRepository;

    public SettlementService(ExpenseService expenseService, UserRepository userRepository) {
        this.expenseService = expenseService;
        this.userRepository = userRepository;
    }

    public static class Transfer {
        public Long fromUserId;
        public Long toUserId;
        public BigDecimal amount;
        public Transfer(Long f, Long t, BigDecimal a) { fromUserId = f; toUserId = t; amount = a; }
    }

    public List<BalanceDTO> balancesForEvent(Long eventId) {
        var map = expenseService.computeBalancesForEvent(eventId);
        List<BalanceDTO> out = new ArrayList<>();
        for (var entry : map.entrySet()) {
            var u = userRepository.findById(entry.getKey()).orElse(null);
            var dto = new BalanceDTO();
            dto.userId = entry.getKey();
            dto.name = (u == null ? "Unknown" : u.getName());
            dto.balance = entry.getValue();
            out.add(dto);
        }
        return out;
    }

    /**
     * Greedy minimal-transfer settlement:
     * - build lists of creditors (positive) and debtors (negative)
     * - repeatedly match largest creditor with largest debtor
     */
    public List<Transfer> suggestSettlements(Long eventId) {
    var map = expenseService.computeBalancesForEvent(eventId);
    // filter near-zero balances
    map.entrySet().removeIf(e -> e.getValue().abs().compareTo(BigDecimal.valueOf(0.01)) < 0);

    // Changed comparison logic: negative balance means they are owed money (creditor)
    PriorityQueue<Map.Entry<Long, BigDecimal>> creditors = new PriorityQueue<>(
            Comparator.comparing((Map.Entry<Long, BigDecimal> e) -> e.getValue())  // Removed .reversed()
    );
    PriorityQueue<Map.Entry<Long, BigDecimal>> debtors = new PriorityQueue<>(
            Comparator.comparing((Map.Entry<Long, BigDecimal> e) -> e.getValue()).reversed()  // Added .reversed()
    );

    // Changed condition: negative balance means they are owed money
    for (var e : map.entrySet()) {
        if (e.getValue().compareTo(BigDecimal.ZERO) < 0) creditors.add(e);  // Changed > to <
        else if (e.getValue().compareTo(BigDecimal.ZERO) > 0) debtors.add(e);  // Changed < to >
    }

    List<Transfer> out = new ArrayList<>();
    while (!creditors.isEmpty() && !debtors.isEmpty()) {
        var c = creditors.poll();
        var d = debtors.poll();
        BigDecimal credit = c.getValue().abs();  // Added .abs()
        BigDecimal debt = d.getValue();
        BigDecimal transfer = credit.min(debt).setScale(2, RoundingMode.HALF_UP);
        
        // Now debtor (positive balance) pays creditor (negative balance)
        out.add(new Transfer(d.getKey(), c.getKey(), transfer));
        
        BigDecimal newCredit = credit.subtract(transfer);
        BigDecimal newDebt = debt.subtract(transfer);
        
        if (newCredit.compareTo(BigDecimal.valueOf(0.01)) >= 0) {
            c.setValue(newCredit.negate());  // Added .negate()
            creditors.add(c);
        }
        if (newDebt.compareTo(BigDecimal.valueOf(0.01)) >= 0) {
            d.setValue(newDebt);
            debtors.add(d);
        }
    }
    return out;
}
}