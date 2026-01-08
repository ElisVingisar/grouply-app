package ee.grouply.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "expense_share")
public class ExpenseShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // owner of this share (who owes this amount)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // store ratio/percentage value as sent by client
    private Double shareValue;

    public ExpenseShare() {}

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Expense getExpense() { return expense; }
    public void setExpense(Expense expense) { this.expense = expense; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Double getShareValue() { return shareValue; }
    public void setShareValue(Double shareValue) { this.shareValue = shareValue; }
}