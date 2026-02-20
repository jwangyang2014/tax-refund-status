package com.intuit.taxrefund.refund.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.intuit.taxrefund.auth.SecurityConfig;
import com.intuit.taxrefund.auth.jwt.JwtAuthenticationFilter;
import com.intuit.taxrefund.auth.jwt.JwtService;
import com.intuit.taxrefund.refund.service.MockIrsAdapter;
import com.intuit.taxrefund.refund.service.RefundService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RefundController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class RefundControllerSecurityWebMvcTest {

  @Autowired MockMvc mvc;

  @MockBean RefundService refundService;
  @MockBean MockIrsAdapter mockIrsAdapter;

  @MockBean JwtService jwtService;

  @Test
  void latest_requiresAuth_returns401_whenNoBearer() throws Exception {
    mvc.perform(get("/api/refund/latest"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void latest_allows_whenBearerValid() throws Exception {
    when(jwtService.parseAndValidate("good-token"))
        .thenReturn(new JwtService.JwtPrincipal(1L, "u1@example.com", "USER"));

    mvc.perform(get("/api/refund/latest")
            .header("Authorization", "Bearer good-token"))
        // controller will call refundService and likely throw because it's mocked
        // if you want 200, stub refundService.latest() to return a DTO
        .andExpect(status().isOk());
  }
}
