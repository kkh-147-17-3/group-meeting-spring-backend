package com.sideprj.groupmeeting.service;

import com.sideprj.groupmeeting.dto.TokenSet;
import com.sideprj.groupmeeting.entity.User;
import com.sideprj.groupmeeting.repository.UserRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${apple.team.id}")
    private String appleTeamId;

    @Value("${apple.key.id}")
    private String appleKeyId;



    @Value("${apple.private_key_path}")
    private String applePrivateKeyPath;

    private static final String APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token";

    private final String appleClientSecret;

    @Autowired
    public AuthService(UserRepository userRepository) throws IOException {
        this.userRepository = userRepository;
        this.appleClientSecret = readP8CertificateAsString(applePrivateKeyPath);
    }

    private static String readP8CertificateAsString(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return new String(Files.readAllBytes(path));
    }


    public TokenSet handleAppleLogin(String code) {
        Map<String, String> data = new HashMap<>();
        data.put("client_id", "com.SideProject.Group");
        data.put("code", code);
        data.put("grant_type", "authorization_code");
        data.put("client_secret", createJWTAppleClientSecret());

        RestTemplate restTemplate = new RestTemplate();
        try {
            AppleGetTokenResponse response = restTemplate.postForObject(APPLE_TOKEN_URL, data, AppleGetTokenResponse.class);
            if (response == null) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Apple login failed");
            }

            Map<String, Object> decodedToken = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(response.getIdToken()).getBody();
            String socialId = (String) decodedToken.get("sub");
            var socialProvider = User.SocialProvider.APPLE;
            String refreshToken = response.getRefreshToken();

            User registeredUser = userRepository.findBySocialProviderAndSocialProviderId(socialProvider, socialId)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setSocialProvider(socialProvider);
                        newUser.setSocialProviderId(socialId);
                        newUser.setAppleRefreshToken(refreshToken);
                        return userRepository.save(newUser);
                    });

            return getTokenSet(registeredUser.getId());
        } catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    public TokenSet getTokenSet(Long userId) {
        return new TokenSet(getUserAccessToken(userId), getUserRefreshToken(userId));
    }

    public String getUserRefreshToken(Long userId) {
        Map<String, Object> claims = new DefaultClaims();
        claims.put("type", "refresh");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    public String getUserAccessToken(Long userId) {
        Map<String, Object> claims = new DefaultClaims();
        claims.put("type", "access");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    private String createJWTAppleClientSecret() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("iss", appleTeamId);
        payload.put("aud", "https://appleid.apple.com");
        payload.put("sub", "com.SideProject.Group");

        return Jwts.builder()
                .setClaims(payload)
                .setHeaderParam("alg", "ES256")
                .setHeaderParam("kid", appleKeyId)
                .setExpiration(new Date(System.currentTimeMillis() + 180L * 24 * 60 * 60 * 1000))
                .signWith(SignatureAlgorithm.ES256, appleClientSecret)
                .compact();
    }

    public String reissueAccessToken(Long userId, String refreshToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(refreshToken);
            return getUserAccessToken(userId);
        } catch (Exception e) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }
}

// Apple login response DTO
@Getter
@Setter
class AppleGetTokenResponse {
    private String idToken;
    private String refreshToken;
}
