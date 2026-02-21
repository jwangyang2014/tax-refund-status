package com.intuit.taxrefund.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.taxrefund.api.ApiError;
import com.intuit.taxrefund.auth.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.Instant;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        JwtAuthenticationFilter jwtAuthenticationFilter,
        ObjectMapper objectMapper
    )
        throws Exception {

        AuthenticationEntryPoint json401EntryPoint = new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request,
                                 HttpServletResponse response,
                                 org.springframework.security.core.AuthenticationException authException)
                throws IOException {

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                ApiError body = new ApiError(
                    Instant.now(),
                    401,
                    "Unauthorized",
                    "Not authenticated",
                    request.getRequestURI());

                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(response.getOutputStream(), body);
            }
        };

        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            // ✅ Return JSON error (with Instant) using Spring's ObjectMapper (has JavaTimeModule)
            .exceptionHandling(eh -> eh.authenticationEntryPoint((req, res, ex) -> {
                writeApiError(objectMapper, res,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Unauthorized",
                    ex.getMessage() != null ? ex.getMessage() : "Unauthorized",
                    req.getRequestURI()
                );
            }))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .anyRequest().authenticated())

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static void writeApiError(
        ObjectMapper objectMapper,
        HttpServletResponse res,
        int status,
        String error,
        String message,
        String path
    ) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiError body = new ApiError(
            Instant.now(),
            status,
            error,
            message,
            path
        );

        objectMapper.writeValue(res.getOutputStream(), body);
    }
}