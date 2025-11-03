package ee.grouply.backend.dto;

public class ShareDTO {
    public Long userId;
    // For PERCENTAGE: percentage value (0-100)
    // For RATIO: relative weight
    // For EQUAL: ignored (server computes)
    public Double value;
}