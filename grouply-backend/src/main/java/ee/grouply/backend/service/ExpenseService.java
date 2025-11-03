package ee.grouply.backend.service;

import ee.grouply.backend.domain.*;
import ee.grouply.backend.dto.*;
import ee.grouply.backend.repo.*;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpenseService {
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final PaymentRepository paymentRepository;

    public ExpenseService(UserRepository userRepository,
                          ExpenseRepository expenseRepository,
                          ExpenseShareRepository shareRepository,
                          PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Expense createExpense(ExpenseCreateDTO dto) {
        var payer = userRepository.findById(dto.payerId)
                .orElseThrow(() -> new IllegalArgumentException("Payer not found"));
        // validate participants exist
        var userIds = dto.shares.stream().map(s -> s.userId).collect(Collectors.toSet());
        var users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) throw new IllegalArgumentException("One or more participants not found");

        Expense e = new Expense();
        e.setEventId(dto.eventId);
        e.setPayer(payer);
        e.setAmount(dto.amount.setScale(2, RoundingMode.HALF_UP));
        e.setDescription(dto.description);
        e.setSplitMode(dto.splitMode);

        // compute share amounts
        List<ExpenseShare> shares = new ArrayList<>();
        if (dto.splitMode == SplitMode.EQUAL) {
            int n = dto.shares.size();
            BigDecimal base = dto.amount.divide(BigDecimal.valueOf(n), 10, RoundingMode.HALF_UP);
            // round to cents and adjust last share for rounding diff
            BigDecimal totalAssigned = BigDecimal.ZERO;
            for (int i = 0; i < dto.shares.size(); i++) {
                var s = dto.shares.get(i);
                BigDecimal amt = base.setScale(2, RoundingMode.HALF_UP);
                if (i == dto.shares.size() - 1) {
                    amt = dto.amount.subtract(totalAssigned).setScale(2, RoundingMode.HALF_UP);
                }
                var share = new ExpenseShare();
                share.setShareValue(null);
                share.setAmount(amt);
                share.setUser(userRepository.getReferenceById(s.userId));
                share.setExpense(e);
                shares.add(share);
                totalAssigned = totalAssigned.add(amt);
            }
        } else if (dto.splitMode == SplitMode.PERCENTAGE) {
            BigDecimal totalAssigned = BigDecimal.ZERO;
            for (int i = 0; i < dto.shares.size(); i++) {
                var s = dto.shares.get(i);
                if (s.value == null) throw new IllegalArgumentException("Missing percentage value for share");
                BigDecimal pct = BigDecimal.valueOf(s.value).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
                BigDecimal amt = dto.amount.multiply(pct);
                amt = amt.setScale(2, RoundingMode.HALF_UP);
                // last share adjust
                if (i == dto.shares.size() - 1) {
                    amt = dto.amount.subtract(totalAssigned).setScale(2, RoundingMode.HALF_UP);
                }
                var share = new ExpenseShare();
                share.setShareValue(s.value);
                share.setAmount(amt);
                share.setUser(userRepository.getReferenceById(s.userId));
                share.setExpense(e);
                shares.add(share);
                totalAssigned = totalAssigned.add(amt);
            }
        } else if (dto.splitMode == SplitMode.RATIO) {
            double totalRatio = dto.shares.stream().mapToDouble(s -> s.value == null ? 0.0 : s.value).sum();
            if (totalRatio <= 0) throw new IllegalArgumentException("Invalid ratio totals");
            BigDecimal totalAssigned = BigDecimal.ZERO;
            for (int i = 0; i < dto.shares.size(); i++) {
                var s = dto.shares.get(i);
                double v = s.value == null ? 0.0 : s.value;
                BigDecimal frac = BigDecimal.valueOf(v).divide(BigDecimal.valueOf(totalRatio), 10, RoundingMode.HALF_UP);
                BigDecimal amt = dto.amount.multiply(frac).setScale(2, RoundingMode.HALF_UP);
                if (i == dto.shares.size() - 1) {
                    amt = dto.amount.subtract(totalAssigned).setScale(2, RoundingMode.HALF_UP);
                }
                var share = new ExpenseShare();
                share.setShareValue(s.value);
                share.setAmount(amt);
                share.setUser(userRepository.getReferenceById(s.userId));
                share.setExpense(e);
                shares.add(share);
                totalAssigned = totalAssigned.add(amt);
            }
        } else {
            throw new IllegalArgumentException("Unsupported split mode");
        }

        e.setShares(shares);
        var saved = expenseRepository.save(e);
        // shares cascade persisted; but ensure shareRepository aware
        return saved;
    }

    public List<Expense> listByEvent(Long eventId) {
        return expenseRepository.findByEventIdOrderByCreatedAtDesc(eventId);
    }

    public Map<Long, BigDecimal> computeBalancesForEvent(Long eventId) {
    Map<Long, BigDecimal> balances = new HashMap<>();
    
    // Get all expenses for this event
    var expenses = expenseRepository.findByEventIdOrderByCreatedAtDesc(eventId);
    
    for (var e : expenses) {
        var payerId = e.getPayer().getId();
        // Change: payer gets positive amount (they paid more than they should)
        balances.putIfAbsent(payerId, BigDecimal.ZERO);
        balances.put(payerId, balances.get(payerId).subtract(e.getAmount()));
        
        // Each participant's share is a negative amount (they owe money)
        for (var s : e.getShares()) {
            var uid = s.getUser().getId();
            balances.putIfAbsent(uid, BigDecimal.ZERO);
            balances.put(uid, balances.get(uid).add(s.getAmount()));
        }
    }
    
    // Payments reduce balances
    var payments = paymentRepository.findByEventId(eventId);
    for (var p : payments) {
        if (!p.isSettled()) continue;
        var from = p.getFromUser().getId();
        var to = p.getToUser().getId();
        balances.putIfAbsent(from, BigDecimal.ZERO);
        balances.putIfAbsent(to, BigDecimal.ZERO);
        // From user reduces their negative balance
        balances.put(from, balances.get(from).subtract(p.getAmount()));
        // To user reduces their positive balance
        balances.put(to, balances.get(to).add(p.getAmount()));
    }
    
    return balances;
}
}