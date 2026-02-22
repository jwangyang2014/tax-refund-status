package com.intuit.taxrefund.assistant.api;

import com.intuit.taxrefund.assistant.api.dto.AssistantChatRequest;
import com.intuit.taxrefund.assistant.api.dto.AssistantChatResponse;
import com.intuit.taxrefund.assistant.api.dto.AssistantChatResponse.Action;
import com.intuit.taxrefund.assistant.api.dto.AssistantChatResponse.ActionType;
import com.intuit.taxrefund.assistant.api.dto.AssistantChatResponse.Citation;
import com.intuit.taxrefund.assistant.api.dto.AssistantChatResponse.Confidence;
import com.intuit.taxrefund.auth.jwt.JwtService;
import com.intuit.taxrefund.refund.api.dto.RefundStatusResponse;
import com.intuit.taxrefund.refund.model.RefundStatus;
import com.intuit.taxrefund.refund.service.RefundService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final RefundService refundService;

    public AssistantController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping("/chat")
    public AssistantChatResponse chat(Authentication auth, @Valid @RequestBody AssistantChatRequest req) {
        JwtService.JwtPrincipal principal = (JwtService.JwtPrincipal) auth.getPrincipal();

        // Pull authoritative data (real)
        RefundStatusResponse refund = refundService.getLatestRefundStatus(principal);

        // Build a deterministic “mock assistant” response (structured)
        String answer = buildAnswer(req.question(), refund);
        List<Citation> citations = buildCitations(refund);
        List<Action> actions = buildActions(refund);
        Confidence confidence = computeConfidence(refund);

        return new AssistantChatResponse(answer, citations, actions, confidence);
    }

    private static String buildAnswer(String question, RefundStatusResponse refund) {
        String status = refund.status();
        String lastUpdated = fmt(refund.lastUpdatedAt());
        String eta = refund.availableAtEstimated() != null ? fmt(refund.availableAtEstimated()) : "unknown";

        StringBuilder sb = new StringBuilder();
        sb.append("**Your question:** ").append(question).append("\n\n");

        sb.append("**Latest refund status:** ").append(status).append("\n");
        sb.append("**Tax year:** ").append(refund.taxYear()).append("\n");
        sb.append("**Last updated:** ").append(lastUpdated).append("\n");

        if (refund.expectedAmount() != null) {
            sb.append("**Expected amount:** $").append(refund.expectedAmount()).append("\n");
        }
        sb.append("**Tracking ID:** ").append(refund.trackingId() != null ? refund.trackingId() : "N/A").append("\n\n");

        RefundStatus st;
        try {
            st = RefundStatus.valueOf(status);
        } catch (Exception e) {
            st = RefundStatus.NOT_FOUND;
        }

        // “Mock reasoning”: status-driven explanation
        switch (st) {
            case AVAILABLE -> {
                sb.append("✅ Your refund is marked **AVAILABLE**. If you don't see it yet in your account, ")
                    .append("it may take additional time for your bank to post it.\n");
            }
            case SENT -> {
                sb.append("📤 Your refund is **SENT**. That typically means the payment has left the processor, ")
                    .append("and your bank may take time to post it.\n");
                sb.append("**Estimated availability:** ").append(eta).append("\n");
            }
            case APPROVED -> {
                sb.append("🟢 Your refund is **APPROVED**. Next, it usually moves to **SENT**.\n");
                sb.append("**Estimated availability:** ").append(eta).append("\n");
            }
            case PROCESSING -> {
                sb.append("⏳ Your refund is **PROCESSING**. During peak season this step can take longer.\n");
                sb.append("**Estimated availability:** ").append(eta).append("\n");
                sb.append("\nIf it remains in PROCESSING for an unusually long time, you may need to verify identity ")
                    .append("or check for IRS notices.\n");
            }
            case RECEIVED -> {
                sb.append("📨 Your refund was **RECEIVED** and is awaiting further processing.\n");
                sb.append("**Estimated availability:** ").append(eta).append("\n");
            }
            case REJECTED -> {
                sb.append("❌ Your refund is **REJECTED**. This usually indicates a mismatch or missing information.\n")
                    .append("Next step: review filing details and any error messages / notices.\n");
            }
            default -> {
                sb.append("I couldn't confidently interpret the current status. Try refreshing, or contact support if it persists.\n");
            }
        }

        // Add a deterministic “timeline sanity” note (interview-friendly)
        if (refund.availableAtEstimated() != null && refund.lastUpdatedAt() != null) {
            long days = Duration.between(Instant.now(), refund.availableAtEstimated()).toDays();
            if (days > 30) {
                sb.append("\n⚠️ Note: The estimate is more than 30 days out, which can happen during peak season or if additional review is required.\n");
            }
        }

        return sb.toString();
    }

    private static List<Citation> buildCitations(RefundStatusResponse refund) {
        // Demo policy snippets (pretend these come from a versioned doc store)
        List<Citation> out = new ArrayList<>();
        out.add(new Citation("IRS_HELP_01", "Refund status updates can be delayed during peak processing periods."));
        out.add(new Citation("IRS_HELP_02", "If a refund is marked SENT, banks may require additional time to post deposits."));
        if ("PROCESSING".equals(refund.status())) {
            out.add(new Citation("IRS_HELP_03", "Long processing times can occur due to verification or return review."));
        }
        if ("REJECTED".equals(refund.status())) {
            out.add(new Citation("IRS_HELP_04", "Rejected refunds often require correcting filing details or addressing notices."));
        }
        return out;
    }

    private static List<Action> buildActions(RefundStatusResponse refund) {
        List<Action> actions = new ArrayList<>();
        actions.add(new Action(ActionType.REFRESH, "Refresh status"));

        if (refund.trackingId() != null && !refund.trackingId().isBlank()) {
            actions.add(new Action(ActionType.SHOW_TRACKING, "Show tracking details"));
        }

        // Escalation action based on status
        if ("REJECTED".equals(refund.status()) || "NOT_FOUND".equals(refund.status())) {
            actions.add(new Action(ActionType.CONTACT_SUPPORT, "Contact support"));
        } else if ("PROCESSING".equals(refund.status())) {
            actions.add(new Action(ActionType.CONTACT_SUPPORT, "Contact support if no update in 21 days"));
        }

        return actions;
    }

    private static Confidence computeConfidence(RefundStatusResponse refund) {
        // Simple heuristic: if we have a computed estimatedAvailableAt, confidence is higher
        if ("AVAILABLE".equals(refund.status())) return Confidence.HIGH;
        if (refund.availableAtEstimated() != null) return Confidence.MEDIUM;
        return Confidence.LOW;
    }

    private static String fmt(Instant ts) {
        if (ts == null) return "N/A";
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(ts);
    }
}