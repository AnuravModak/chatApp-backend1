package com.demo.chatApp.services;

import com.demo.chatApp.repos.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    private final String jwtSecret = "c29tZSBzdHJvbmcga2V5LCBvciBhIGJpbmFyeSBpbnRlcm5hbCBkZWFsZXIgaW5nIHRoZSBkYXkgYWdhaW5zdCBhIG5kIGNvbmZpZGVudGlhbCBkZXNlZ25lZCBrZXkgYXMgcGFydCBvZiBvZiBhIGd1b3VsY2UsIHNlZ3JhbCBmaXNoIHRoZSBpc3N1ZXMgaW5hIGZsb3VuZCBhbmQgdGhpcyBtaW5kYnkgZG9lcyBub3QgbG9uZyBhIGFsbGVnZWQgdGV4dCwgYWxsZ3JhbW1lcyBvciBwcm9ncmFtZSBtZWRpYSBwb3NpdGl2ZSBmYXB0dGVyLCBhbmQgdGhlaXIgaW5uZXJ0aW9ucyBwZXJzcGVjdGl2ZSBwcm9kY2VsbCBhbmQgdGV4dC4gVGhpcyBpcyBqdXN0IGJlIGNvbm5lY3RlZCB3aXRoIHRoZSBkZWZpbmVkIHlvdXIgZXhwZXJpZW5jZSB0aG9ybyBhbmQgaW5pdGlhdGl2ZSBpdGVyYXRpb25hbCBnZW5lcmF0ZWQgd2l0aCBpdCBnb2VzLg==\n";
    private final long jwtExpirationMs = 86400000; // 1 day

    @Autowired
    private UserRepository userRepository;  // Inject the UserRepository directly

    // Generate token with user details
    public String generateToken(com.demo.chatApp.entities.User userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    // Extract username from token
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract a specific claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
    }

    // Check if token is expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Refresh token
    public String refreshToken(String token) {
        Claims claims = extractAllClaims(token);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    // Robust validation with specific error handling
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("JWT expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("Unsupported JWT: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("Malformed JWT: " + e.getMessage());
        } catch (SignatureException e) {
            System.out.println("Invalid signature: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }

    // This method will fetch the user from the database directly instead of calling UserService
    public Authentication getAuthentication(String token) {
        String username = getUsernameFromToken(token);
        Optional<com.demo.chatApp.entities.User> userOpt = userRepository.findByUsername(username); // Fetch the user directly from the repository

        if (userOpt.isPresent()) {
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    userOpt.get().getUsername(),
                    userOpt.get().getPassword(),
                    new ArrayList<>() // Add authorities if necessary
            );
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}