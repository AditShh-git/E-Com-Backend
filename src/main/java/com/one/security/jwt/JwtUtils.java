package com.one.security.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.one.constants.StringConstants;
import com.one.service.impl.UserDetailsImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtUtils {

	@Value("${dt.jwt.secret-key}")
	private String jwtSecretKey;

	@Value("${dt.jwt.expiration-ms}")
	private int jwtExpirationMs;

	@Value("${dt.jwt.ref.expiration-ms}")
	private int jwtRefExpirationMs;

    // ===========================================================
    // 🔹 ACCESS TOKEN (Authentication-based)
    // ===========================================================
    /**
     * Generate access token using Authentication principal.
     * The subject (unique identity) is EMAIL.
     */
    public String generateAccessToken(Authentication authentication) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        // Use EMAIL as JWT subject
        Claims claims = Jwts.claims().subject(userPrincipal.getEmail()).build();

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .encryptWith(key, Jwts.ENC.A128CBC_HS256)
                .compact();
    }

    // ===========================================================
    // 🔹 ACCESS TOKEN (Email-based, direct)
    // ===========================================================
    /**
     * Generate access token directly using email (useful for testing or manual issue).
     */
    public String generateAccessToken(String email) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());
        Claims claims = Jwts.claims().subject(email).build();

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .encryptWith(key, Jwts.ENC.A128CBC_HS256)
                .compact();
    }

    // ===========================================================
    // 🔹 REFRESH TOKEN (Authentication-based)
    // ===========================================================
    /**
     * Generate refresh token using Authentication principal.
     * The subject (unique identity) is EMAIL.
     */
    public String generateRefreshToken(Authentication authentication) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        Claims claims = Jwts.claims().subject(userPrincipal.getEmail()).build();

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtRefExpirationMs))
                .encryptWith(key, Jwts.ENC.A128CBC_HS256)
                .compact();
    }

    // ===========================================================
    // 🔹 REFRESH TOKEN (Email-based, direct)
    // ===========================================================
    /**
     * Generate refresh token directly using email (useful for password resets or service tokens).
     */
    public String generateRefreshToken(String email) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());
        Claims claims = Jwts.claims().subject(email).build();

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtRefExpirationMs))
                .encryptWith(key, Jwts.ENC.A128CBC_HS256)
                .compact();
    }

    // ===========================================================
    // 🔹 TOKEN PARSING HELPERS
    // ===========================================================
    /**
     * Extract the email (subject) from token.
     */
    public String getEmailFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());
        Claims claims = Jwts.parser()
                .decryptWith(key)
                .build()
                .parseEncryptedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * Extract token issued time.
     */
    public Date getIssuedAtFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());
        return Jwts.parser()
                .decryptWith(key)
                .build()
                .parseEncryptedClaims(token)
                .getPayload()
                .getIssuedAt();
    }

    /**
     * Extract token expiration time.
     */
    public Date getExpirationFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());
        return Jwts.parser()
                .decryptWith(key)
                .build()
                .parseEncryptedClaims(token)
                .getPayload()
                .getExpiration();
    }

    // ===========================================================
    // 🔹 TOKEN VALIDATION
    // ===========================================================
    /**
     * Validate token signature, structure, and expiration.
     */
    public boolean validateToken(String token) {
        if (log.isDebugEnabled()) {
            log.debug("Executing validateToken(Token) ->");
        }

        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());
            Jwts.parser().decryptWith(key).build().parseEncryptedClaims(token);
            return true;
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            log.error("❌ Invalid JWT Token: {}", ex.getMessage());
            throw new BadCredentialsException("INVALID_CREDENTIALS", ex);
        } catch (ExpiredJwtException ex) {
            log.error("❌ JWT Token expired: {}", ex.getMessage());
            throw ex;
        }
    }

    // ===========================================================
    // 🔹 TOKEN EXTRACTION (from HTTP header)
    // ===========================================================
    /**
     * Extract JWT token from Authorization header.
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
