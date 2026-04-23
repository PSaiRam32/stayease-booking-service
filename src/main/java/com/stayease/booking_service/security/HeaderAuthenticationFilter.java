package com.stayease.booking_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final String SECRET = "stayease-super-secret-key-for-jwt-authentication-2026-secure-key";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/auth/register",
            "/auth/refresh-token",
            "/owners/auth-internal",
            "/owners/owners-internal/**"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        log.debug("Processing request for path: {}", path);

        if (PUBLIC_PATHS.stream().anyMatch(path::contains)) {
            log.debug("Request to public path, bypassing authentication: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");

        if (userId != null && role != null) {
            log.info("Authentication headers found - User ID: {}, Role: {}", userId, role);
            setAuthentication(userId, role);
        } else {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                log.debug("Bearer token found in Authorization header");
                String token = authHeader.substring(7);
                try {
                    Claims claims = Jwts.parser()
                            .setSigningKey(SECRET.getBytes())
                            .parseClaimsJws(token)
                            .getBody();
                    String extractedUserId = claims.getSubject();
                    String extractedRole = claims.get("role", String.class);
                    log.info("JWT token parsed successfully - User ID: {}, Role: {}", extractedUserId, extractedRole);
                    setAuthentication(extractedUserId, extractedRole);
                } catch (Exception e) {
                    log.error("JWT token parsing failed: {}", e.getMessage());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            } else {
                log.warn("No authentication headers or bearer token found for path: {}", path);
            }
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String userId, String role) {
        log.debug("Setting authentication for user ID: {} with role: {}", userId, role);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                );
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.info("Authentication set successfully for user ID: {}", userId);
    }
}