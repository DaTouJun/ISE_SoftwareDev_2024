package org.flitter.backend.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {
        String expiredMessage = (String) request.getAttribute("tokenExpired");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        if (expiredMessage != null) {
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \""
                    + expiredMessage + "\"}");
        } else {
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"You are not login\"}");
        }
    }
}
