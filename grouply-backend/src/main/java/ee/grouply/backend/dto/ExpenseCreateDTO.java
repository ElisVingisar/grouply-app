package ee.grouply.backend.dto;

import java.math.BigDecimal;
import java.util.List;

import ee.grouply.backend.entity.SplitMode;

public class ExpenseCreateDTO {
    public Long eventId;
    public Long payerId;
    public BigDecimal amount;
    public String description;
    public SplitMode splitMode;
    public List<ShareInput> shares;

    public static class ShareInput {
        public Long userId;
        public Double value;  // percentage, ratio, or exact amount depending on mode
    }
}