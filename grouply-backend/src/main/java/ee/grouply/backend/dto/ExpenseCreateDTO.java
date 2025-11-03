package ee.grouply.backend.dto;

import ee.grouply.backend.domain.SplitMode;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class ExpenseCreateDTO {
    @NotNull
    public Long eventId;

    @NotNull
    public Long payerId;

    @NotNull
    @DecimalMin("0.01")
    public BigDecimal amount;

    public String description;

    @NotNull
    public SplitMode splitMode;

    @NotEmpty
    public List<ShareDTO> shares;
}