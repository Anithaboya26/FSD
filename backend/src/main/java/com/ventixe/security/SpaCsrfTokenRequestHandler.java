package com.ventixe.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import java.util.function.Supplier;

public final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

  private final CsrfTokenRequestHandler xorHandler = new XorCsrfTokenRequestAttributeHandler();
  private final CsrfTokenRequestHandler plainHandler = new CsrfTokenRequestAttributeHandler();

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      Supplier<CsrfToken> csrfToken) {
    xorHandler.handle(request, response, csrfToken);
    csrfToken.get();
  }

  @Override
  public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
    String headerValue = request.getHeader(csrfToken.getHeaderName());
    return headerValue == null || headerValue.isBlank()
        ? xorHandler.resolveCsrfTokenValue(request, csrfToken)
        : plainHandler.resolveCsrfTokenValue(request, csrfToken);
  }
}
