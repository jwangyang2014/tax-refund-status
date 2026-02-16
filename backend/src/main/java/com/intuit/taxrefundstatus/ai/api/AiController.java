package com.intuit.taxrefundstatus.ai.api;

import com.intuit.taxrefundstatus.ai.model.AiRequestLog;
import com.intuit.taxrefundstatus.ai.repo.AiRequestLogRepository;
import com.intuit.taxrefundstatus.api.dto.FeedbackRequest;
import com.intuit.taxrefundstatus.auth.jwt.JwtService;
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
