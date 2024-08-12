package com.sideprj.groupmeeting.jwt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sideprj.groupmeeting.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtProvider {

    private final byte[] secretKey;
    private final long accessTokenValidTime = Duration.ofMinutes(30000000).toMillis(); // 30 minutes expiration
    private final long refreshTokenValidTime = Duration.ofMinutes(10).toMillis(); // 10 minutes expiration (was 2 weeks in Kotlin version)

    private final UserDetailsService userDetailsService;

    private static final ObjectMapper mapper = new ObjectMapper();


    public JwtProvider(@Value("${jwt.secret}") String secretKey, UserDetailsService userDetailsService, ObjectMapper mapper) {
        this.secretKey = secretKey.getBytes();
        this.userDetailsService = userDetailsService;
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }

        // Extract token
        return authorization.split("\\s+")[1];
    }

    public Long getUserId(String token) {
        return Long.parseLong(Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject());
    }

    public String getLoginType(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .get("loginType", String.class);
    }

    public boolean isExpired(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getHeader()
                .get("type"));
    }

    public boolean isAccessToken(String token) {
        return "access".equals(Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .get("type"));
    }

    public String createAccessToken(Long userId, User.SocialProvider  loginType) {
        return createJwt(userId, loginType.name(), "access", accessTokenValidTime);
    }

    public String createRefreshToken(Long userId, User.SocialProvider loginType) {
        return createJwt(userId, loginType.name(), "refresh", refreshTokenValidTime);
    }

    private String createJwt(Long userId, String loginType, String type, long tokenValidTime) {
        var claims = Jwts.claims();
        claims.put("userId", userId);
        claims.put("loginType", loginType);
        return Jwts.builder()
                .setHeaderParam("type", type)
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenValidTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        String userId = getUserId(token).toString();
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public static Map<String, Object> parseJwtWithoutValidation(String token) {
        try {
            String[] chunks = token.split("\\.");

            if (chunks.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }

            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));

            TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {};

            return mapper.readValue(payload, typeReference);
        } catch (Exception e) {
            // Handle exception (e.g., log it or throw a custom exception)
            System.err.println("Error parsing JWT: " + e.getMessage());
            return null;
        }
    }
}