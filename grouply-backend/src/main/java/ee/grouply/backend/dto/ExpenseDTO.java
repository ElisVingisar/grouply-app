package ee.grouply.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class ExpenseDTO {
    public Long id;
    public Long eventId;
    public Long payerId;
    public String payerName;
    public BigDecimal amount;
    public String description;
    public String splitMode;
    public OffsetDateTime createdAt;
    public List<ShareView> shares;

    public static class ShareView {
        public Long userId;
        public String userName;
        public BigDecimal amount;
    }
}