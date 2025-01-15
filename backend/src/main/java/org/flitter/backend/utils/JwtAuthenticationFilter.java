package org.flitter.backend.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.flitter.backend.service.CustomUserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class JwtAuthenticationFilter extends BasicAuthenticationFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
                                   JwtTokenProvider tokenProvider,
                                   CustomUserDetailsService customUserDetailsService1) {
        super(authenticationManager);
        this.jwtTokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService1;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws java.io.IOException, jakarta.servlet.ServletException {
        if (request.getRequestURI().contains("/api/auth/refresh")) {    // 刷新token的接口跳过access鉴权
            chain.doFilter(request, response); // 跳过验证
            return;
        }

        String token = getJwtFromRequest(request);

        if (token != null && !token.isEmpty() && jwtTokenProvider.validateToken(token, true, request)) {
            String username = jwtTokenProvider.getUsernameFromJwt(token, true);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null,
                            userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
