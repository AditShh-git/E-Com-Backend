package com.one.security.jwt;

import java.io.IOException;

import com.one.aim.repo.AdminRepo;
import com.one.aim.repo.SellerRepo;
import com.one.aim.repo.UserRepo;
import com.one.aim.repo.VendorRepo;
import com.one.service.impl.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {


	private final JwtUtils jwtUtils;
	private final UserDetailsServiceImpl userDetailsService;
    private final UserRepo userRepo;
    private final AdminRepo adminRepo;
    private final SellerRepo sellerRepo;
    private final VendorRepo vendorRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String path = request.getRequestURI();

            // Public routes — skip auth
            if (path.contains("/api/auth/signin") ||
                    path.contains("/api/auth/signup") ||
                    path.contains("/api/auth/refresh") ||
                    path.contains("/api/auth/forgot-password")) {

                filterChain.doFilter(request, response);
                return;
            }

            // Extract JWT
            String token = getTokenFromRequest(request);

            if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {

                String email = jwtUtils.getEmailFromToken(token);

                if (email != null &&
                        SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Load UserDetails (User/Seller/Admin/Vendor)
                    UserDetailsImpl userDetails =
                            (UserDetailsImpl) userDetailsService.loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Store authentication in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.info("Authenticated [{}] ({})", userDetails.getRole(), email);
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Unexpected error in JWT filter: {}", e.getMessage());
            filterChain.doFilter(request, response);

        }
    }



    // ===========================================================
    // Handle Expired Tokens (for Refresh Endpoint)
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
    //  Extract Token from Authorization Header
    // ===========================================================
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}