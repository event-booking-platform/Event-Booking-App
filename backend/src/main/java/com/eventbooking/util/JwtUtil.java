package com.eventbooking.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final String secretString = "BookEasySecretKeyForJWTGeneration2024EventBookingPlatformSecure";
    private final Key key = Keys.hmacShaKeyFor(secretString.getBytes());
    private final int jwtExpirationMs = 86400000;

    public JwtUtil() {
        System.out.println(" JwtUtil initialized with FIXED secret key");
        System.out.println("Key length: " + secretString.length());
    }

    public String generateToken(String username) {
        System.out.println("Generating token for user: " + username);
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key)
                .compact();
        System.out.println(" Token generated successfully");
        return token;
    }

    public String getUsernameFromToken(String token) {
        try {
            System.out.println("Parsing token...");
            String username = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            System.out.println("Token parsed successfully for user: " + username);
            return username;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println(" JWT parsing error: " + e.getMessage());
            throw e;
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            boolean isValid = (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
            System.out.println(" Token validation result: " + isValid);
            return isValid;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println(" JWT validation error: " + e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private Date getExpirationDateFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println(" JWT expiration parsing error: " + e.getMessage());
            throw e;
        }
    }
}