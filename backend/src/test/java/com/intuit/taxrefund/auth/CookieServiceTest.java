package com.intuit.taxrefund.auth;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class CookieServiceTest {

  @Test
  void setRefreshCookie_writesSetCookieHeader_withExpectedAttributes() {
    CookieProps props = new CookieProps("refresh_token", false, "Lax");
    CookieService svc = new CookieService(props);

    MockHttpServletResponse res = new MockHttpServletResponse();
    svc.setRefreshCookie(res, "refresh.value", Duration.ofDays(14).toSeconds());

    String header = res.getHeader("Set-Cookie");
    assertNotNull(header);

    assertTrue(header.contains("refresh_token=refresh.value"));
    assertTrue(header.contains("HttpOnly"));
    assertTrue(header.contains("Path=/api/auth/refresh"));
    assertTrue(header.contains("SameSite=Lax"));
    assertTrue(header.contains("Max-Age="));
    assertFalse(header.contains("Secure"), "secure=false should not set Secure");
  }

  @Test
  void clearRefreshCookie_setsMaxAge0() {
    CookieProps props = new CookieProps("refresh_token", false, "Lax");
    CookieService svc = new CookieService(props);

    MockHttpServletResponse res = new MockHttpServletResponse();
    svc.clearRefreshCookie(res);

    String header = res.getHeader("Set-Cookie");
    assertNotNull(header);
    assertTrue(header.contains("Max-Age=0"));
  }
}
