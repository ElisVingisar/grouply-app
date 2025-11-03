package ee.grouply.backend.dto;

import java.math.BigDecimal;

public class BalanceDTO {
    public Long userId;
    public String name;
    public BigDecimal balance; // positive = they are owed, negative = they owe
}