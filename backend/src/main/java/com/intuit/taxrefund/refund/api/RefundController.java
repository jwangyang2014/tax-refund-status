package com.intuit.taxrefund.refund.api;

import com.intuit.taxrefund.auth.jwt.JwtService;
import com.intuit.taxrefund.refund.api.dto.RefundStatusInternalUpdateRequest;
import com.intuit.taxrefund.refund.api.dto.RefundStatusResponse;
import com.intuit.taxrefund.refund.service.IrsAdapter;
import com.intuit.taxrefund.refund.service.MockIrsAdapter;
import com.intuit.taxrefund.refund.service.RefundService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/refund")
public class RefundController {
    private final RefundService refundService;
    private final MockIrsAdapter mockIrs;

    public RefundController(RefundService refundService, MockIrsAdapter mockIrs) {
        this.refundService = refundService;
        this.mockIrs = mockIrs;
    }

    @GetMapping("/latest")
    public RefundStatusResponse latest(Authentication auth) {
        JwtService.JwtPrincipal principal = (JwtService.JwtPrincipal) auth.getPrincipal();
        return refundService.getLatestRefundStatus(principal);
    }

    // Demo/testing endpoint to trigger a status change in the mock IRS adapter
    @PostMapping("/simulate")
    public void simulate(Authentication auth, @Valid @RequestBody RefundStatusInternalUpdateRequest req) {
        JwtService.JwtPrincipal principal = (JwtService.JwtPrincipal) auth.getPrincipal();
        mockIrs.upsert(
            principal.userId(),
            new IrsAdapter.IrsRefundResult(
                req.taxYear(), req.statusEnum(), req.expectedAmount(), req.trackingId()
            )
        );
    }
}
