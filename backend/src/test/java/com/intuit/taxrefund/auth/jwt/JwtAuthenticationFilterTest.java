package com.intuit.taxrefund.auth.jwt;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

  @AfterEach
  void cleanup() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilter_setsSecurityContext_whenBearerTokenValid() throws Exception {
    JwtService jwtService = mock(JwtService.class);
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

    when(jwtService.parseAndValidate("good-token"))
        .thenReturn(new JwtService.JwtPrincipal(1L, "u1@example.com", "USER"));

    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer good-token");
    MockHttpServletResponse res = new MockHttpServletResponse();
    FilterChain chain = new MockFilterChain();

    filter.doFilter(req, res, chain);

    var auth = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(auth, "Authentication should be set");
    assertTrue(auth.isAuthenticated());
    assertEquals("u1@example.com", ((JwtService.JwtPrincipal) auth.getPrincipal()).email());
    assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
  }

  @Test
  void doFilter_doesNothing_whenNoAuthorizationHeader() throws Exception {
    JwtService jwtService = mock(JwtService.class);
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

    MockHttpServletRequest req = new MockHttpServletRequest();
    MockHttpServletResponse res = new MockHttpServletResponse();
    FilterChain chain = new MockFilterChain();

    filter.doFilter(req, res, chain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(jwtService, never()).parseAndValidate(anyString());
  }

  @Test
  void doFilter_clearsSecurityContext_whenTokenInvalid() throws Exception {
    JwtService jwtService = mock(JwtService.class);
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

    when(jwtService.parseAndValidate("bad-token"))
        .thenThrow(new RuntimeException("bad"));

    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer bad-token");
    MockHttpServletResponse res = new MockHttpServletResponse();
    FilterChain chain = new MockFilterChain();

    filter.doFilter(req, res, chain);

    assertNull(SecurityContextHolder.getContext().getAuthentication(), "Should not authenticate invalid token");
  }
}
