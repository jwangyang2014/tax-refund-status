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
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
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

                // return JSON instead of empty 401
                .exceptionHandling(eh -> eh.authenticationEntryPoint(json401EntryPoint))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        .anyRequest().authenticated())

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}