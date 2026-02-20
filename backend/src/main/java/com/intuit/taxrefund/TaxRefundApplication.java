package com.intuit.taxrefund;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.intuit.taxrefund.auth.CookieProps;

@SpringBootApplication
@EnableConfigurationProperties(CookieProps.class)
public class TaxRefundApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaxRefundApplication.class, args);
    }
}
