package ee.grouply.backend.api;

import ee.grouply.backend.dto.BalanceDTO;
import ee.grouply.backend.service.SettlementService;
import ee.grouply.backend.service.SettlementService.Transfer;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/events")
public class GroupController {
    private final SettlementService settlementService;

    public GroupController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    // balances for event (used as group)
    @GetMapping("/{id}/balances")
    public List<BalanceDTO> balances(@PathVariable("id") Long eventId) {
        return settlementService.balancesForEvent(eventId);
    }

    @GetMapping("/{id}/settlements/suggested")
    public List<Object> suggested(@PathVariable("id") Long eventId) {
        List<Transfer> transfers = settlementService.suggestSettlements(eventId);
        return transfers.stream().map(t -> Map.of(
                "fromUserId", t.fromUserId,
                "toUserId", t.toUserId,
                "amount", t.amount
        )).collect(Collectors.toList());
    }
}