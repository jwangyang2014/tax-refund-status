package com.intuit.taxrefund.ai.api;

import com.intuit.taxrefund.ai.model.AiRequestLog;
import com.intuit.taxrefund.ai.repo.AiRequestLogRepository;
import com.intuit.taxrefund.api.dto.FeedbackRequest;
import com.intuit.taxrefund.auth.jwt.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiRequestLogRepository requestLogRepo;

    public AiController(AiRequestLogRepository requestLogRepo) {
        this.requestLogRepo = requestLogRepo;
    }

    @PostMapping("/feedback/latest")
    public void feedbackLatest(Authentication auth, @RequestBody FeedbackRequest req) {
        JwtService.JwtPrincipal principal = (JwtService.JwtPrincipal) auth.getPrincipal();
        AiRequestLog log = requestLogRepo.findTopByUserIdOrderByCreatedAtDesc(principal.userId())
            .orElseThrow(() -> new IllegalArgumentException("No AI run found yet for user"));
        log.setHelpful(req.helpful());
        requestLogRepo.save(log);
    }
}
