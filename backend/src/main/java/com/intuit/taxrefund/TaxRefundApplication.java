package com.intuit.taxrefund;

import com.intuit.taxrefund.ml.MlProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.intuit.taxrefund.auth.CookieProps;
import com.intuit.taxrefund.ratelimit.RateLimitProps;

@SpringBootApplication
@EnableConfigurationProperties({ CookieProps.class, RateLimitProps.class, MlProps.class })
public class TaxRefundApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaxRefundApplication.class, args);
    }
}
