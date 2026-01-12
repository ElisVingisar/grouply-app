package ee.grouply.backend.service;

import ee.grouply.backend.dto.*;
import ee.grouply.backend.entity.*;
import ee.grouply.backend.repo.*;
import ee.grouply.backend.error.ForbiddenException;
import ee.grouply.backend.error.NotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ExpenseService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final PaymentRepository paymentRepository;
    private final EventRepository eventRepository;

    public ExpenseService(UserRepository userRepository,
                          ExpenseRepository expenseRepository,
                          ExpenseShareRepository shareRepository,
                          PaymentRepository paymentRepository,
                          EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.paymentRepository = paymentRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public ExpenseDTO createExpense(ExpenseCreateDTO dto, String userEmail) {
        log.debug("Creating expense for event {}, payer {}, by user {}", 
                dto.eventId, dto.payerId, userEmail);

        checkEventAccess(dto.eventId, userEmail);

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
        } else if (dto.splitMode == SplitMode.EXACT) {
            double totalRatio = dto.shares.stream().mapToDouble(s -> s.value == null ? 0.0 : s.value).sum();
            if (totalRatio <= 0) throw new IllegalArgumentException("Invalid exact amounts");
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

        log.info("Expense {} created for event {}", saved.getId(), dto.eventId);
        return toDTO(saved);
    }

    public List<ExpenseDTO> listByEvent(Long eventId, String userEmail) {

        checkEventAccess(eventId, userEmail);

        return expenseRepository.findByEventIdOrderByCreatedAtDesc(eventId)
                .stream()
                .map(this::toDTO)
                .toList();
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

    /**
     * Update an existing expense
     */
    @Transactional
    public ExpenseDTO updateExpense(Long expenseId, ExpenseCreateDTO dto, String userEmail) {
        log.debug("Updating expense {} by user {}", expenseId, userEmail);

        checkEventAccess(dto.eventId, userEmail);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found"));

        User payer = userRepository.findById(dto.payerId)
                .orElseThrow(() -> new NotFoundException("Payer not found"));

        // Update basic fields
        expense.setDescription(dto.description);
        expense.setAmount(dto.amount.setScale(2, RoundingMode.HALF_UP));  // scale to 2 decimals
        expense.setSplitMode(dto.splitMode);  // dto.splitMode is already SplitMode enum
        expense.setPayer(payer);

        // Remove old shares
        expense.getShares().clear();

        // Add new shares (values will be recalculated by calculateShares)
        for (ExpenseCreateDTO.ShareInput s : dto.shares) {
            User u = userRepository.findById(s.userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            ExpenseShare share = new ExpenseShare();
            share.setExpense(expense);
            share.setUser(u);
            share.setShareValue(s.value);
            share.setAmount(BigDecimal.ZERO); // Will be calculated below
            expense.getShares().add(share);
        }

        // Recalculate share amounts
        calculateShares(expense);

        return toDTO(expenseRepository.save(expense));
    }

    /**
     * Delete an expense
     */
    @Transactional
    public void deleteExpense(Long expenseId, Long eventId, String userEmail) {
        log.debug("Deleting expense {} by user {}", expenseId, userEmail);

        checkEventAccess(eventId, userEmail);

        if (!expenseRepository.existsById(expenseId)) {
            throw new NotFoundException("Expense not found");
        }
        expenseRepository.deleteById(expenseId);
        log.info("Expense {} deleted", expenseId);
    }

    private void calculateShares(Expense e) {
        // compute share amounts
        if (e.getSplitMode() == SplitMode.EQUAL) {
            int n = e.getShares().size();
            BigDecimal base = e.getAmount().divide(BigDecimal.valueOf(n), 10, RoundingMode.HALF_UP);
            // round to cents and adjust last share for rounding diff
            BigDecimal totalAssigned = BigDecimal.ZERO;
            for (int i = 0; i < e.getShares().size(); i++) {
                var s = e.getShares().get(i);
                BigDecimal amt = base.setScale(2, RoundingMode.HALF_UP);
                if (i == e.getShares().size() - 1) {
                    amt = e.getAmount().subtract(totalAssigned).setScale(2, RoundingMode.HALF_UP);
                }
                s.setAmount(amt);
                totalAssigned = totalAssigned.add(amt);
            }
        } else if (e.getSplitMode() == SplitMode.PERCENTAGE) {
            BigDecimal totalAssigned = BigDecimal.ZERO;
            for (int i = 0; i < e.getShares().size(); i++) {
                var s = e.getShares().get(i);
                if (s.getShareValue() == null) throw new IllegalArgumentException("Missing percentage value for share");
                BigDecimal pct = BigDecimal.valueOf(s.getShareValue()).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
                BigDecimal amt = e.getAmount().multiply(pct);
                amt = amt.setScale(2, RoundingMode.HALF_UP);
                // last share adjust
                if (i == e.getShares().size() - 1) {
                    amt = e.getAmount().subtract(totalAssigned).setScale(2, RoundingMode.HALF_UP);
                }
                s.setAmount(amt);
                totalAssigned = totalAssigned.add(amt);
            }
        } else if (e.getSplitMode() == SplitMode.EXACT) {  // changed from RATIO
            double totalRatio = e.getShares().stream().mapToDouble(s -> s.getShareValue() == null ? 0.0 : s.getShareValue()).sum();
            if (totalRatio <= 0) throw new IllegalArgumentException("Invalid exact amounts");
            BigDecimal totalAssigned = BigDecimal.ZERO;
            for (int i = 0; i < e.getShares().size(); i++) {
                var s = e.getShares().get(i);
                double v = s.getShareValue() == null ? 0.0 : s.getShareValue();
                BigDecimal frac = BigDecimal.valueOf(v).divide(BigDecimal.valueOf(totalRatio), 10, RoundingMode.HALF_UP);
                BigDecimal amt = e.getAmount().multiply(frac).setScale(2, RoundingMode.HALF_UP);
                if (i == e.getShares().size() - 1) {
                    amt = e.getAmount().subtract(totalAssigned).setScale(2, RoundingMode.HALF_UP);
                }
                s.setAmount(amt);
                totalAssigned = totalAssigned.add(amt);
            }
        } else {
            throw new IllegalArgumentException("Unsupported split mode");
        }
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

    // ══════════════════════════════════════════════════════════════════
    // Authorization helpers
    // ══════════════════════════════════════════════════════════════════

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
    }

    private void checkEventAccess(Long eventId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Event event = findEventById(eventId);
        
        boolean isCreator = event.getCreator() != null 
                && event.getCreator().getId().equals(user.getId());
        boolean isParticipant = event.getParticipants().contains(user);
        
        if (!isCreator && !isParticipant) {
            log.warn("User {} denied access to event {}", userEmail, eventId);
            throw new ForbiddenException("You don't have access to this event");
        }
    }
}