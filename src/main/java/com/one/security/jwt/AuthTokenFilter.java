package com.one.security.jwt;

import java.io.IOException;

import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.one.aim.constants.ErrorCodes;
import com.one.constants.StringConstants;
import com.one.service.impl.UserDetailsServiceImpl;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String path = request.getRequestURI();

            // ===========================================================
            // 🔹 Skip authentication for public endpoints
            // ===========================================================
            if (path.contains("/api/auth/signin") ||
                    path.contains("/api/auth/signup") ||
                    path.contains("/api/auth/refresh") ||
                    path.contains("/api/auth/forgot-password")) {

                filterChain.doFilter(request, response);
                return;
            }

            // ===========================================================
            // 🔹 Extract Bearer Token from Authorization header
            // ===========================================================
            String token = getTokenFromRequest(request);
            if (StringUtils.hasText(token)) {

                // 🔸 Validate & decrypt (AES-based, no signature check)
                if (jwtUtils.validateToken(token)) {

                    // ===========================================================
                    // 🔹 Extract EMAIL (subject) from decrypted token
                    // ===========================================================
                    String email = jwtUtils.getEmailFromToken(token);

                    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.info("✅ Authenticated user '{}' via encrypted JWT", email);
                    }
                }
            }

        } catch (ExpiredJwtException ex) {
            log.warn("⚠️ Token expired: {}", ex.getMessage());
            handleExpiredToken(ex, request);

        } catch (BadCredentialsException ex) {
            log.error("❌ Invalid credentials in token: {}", ex.getMessage());
            request.setAttribute(StringConstants.JWT_BAD_CREDENTIALS, ErrorCodes.EC_INVALID_TOKEN);

        } catch (JwtException ex) {
            log.error("❌ Invalid or malformed token: {}", ex.getMessage());
            request.setAttribute(StringConstants.JWT_INVALID_TOKEN, ErrorCodes.EC_INVALID_TOKEN);

        } catch (Exception e) {
            log.error("❌ Unexpected authentication error: {}", e.getMessage());
            request.setAttribute(StringConstants.JWT_INVALID_TOKEN, ErrorCodes.EC_INVALID_TOKEN);
        }

        // Proceed with filter chain
        filterChain.doFilter(request, response);
    }

    // ===========================================================
    // 🔹 Handle Expired Tokens (for Refresh Endpoint)
    // ===========================================================
    private void handleExpiredToken(ExpiredJwtException ex, HttpServletRequest request) {
        String isRefreshToken = request.getHeader("isRefreshToken");
        String requestURL = request.getRequestURL().toString();

        if ("true".equals(isRefreshToken) && requestURL.contains("refresh")) {
            // Allow temporary authentication for token renewal
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(null, null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("claims", ex.getClaims());
        } else {
            request.setAttribute(StringConstants.JWT_INVALID_TOKEN, ErrorCodes.EC_INVALID_TOKEN);
        }
    }

    // ===========================================================
    // 🔹 Extract Token from Authorization Header
    // ===========================================================
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}