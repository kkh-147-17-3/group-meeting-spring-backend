package com.sideprj.groupmeeting.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sideprj.groupmeeting.dto.TokenSet;
import com.sideprj.groupmeeting.entity.User;
import com.sideprj.groupmeeting.exceptions.BadRequestException;
import com.sideprj.groupmeeting.exceptions.UnauthorizedException;
import com.sideprj.groupmeeting.jwt.JwtProvider;
import com.sideprj.groupmeeting.repository.UserRepository;
import io.jsonwebtoken.JwsHeader;
import lombok.Getter;
import lombok.Setter;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.HttpClientErrorException;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.*;

import okhttp3.*;

import static com.sideprj.groupmeeting.jwt.JwtProvider.parseJwtWithoutValidation;

@Service
public class AuthService {

    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${apple.team.id}")
    private String appleTeamId;

    @Value("${apple.key.id}")
    private String appleKeyId;

    private static final long ACCESS_TOKEN_EXPIRES_IN =  60L * 60 * 1000;
    private static final long REFRESH_TOKEN_EXPIRES_IN = 30L * 24 * 60 * 60 * 1000;

    private static final String APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token";

    private final PrivateKey applePrivateKey;

    private final OkHttpClient client;

    private final ObjectMapper mapper;

    private final JwtProvider jwtProvider;

    @Autowired
    public AuthService(
            UserRepository userRepository,
            ObjectMapper mapper,
            OkHttpClient client,
            @Value("${apple.private_key_path}") String applePrivateKeyPath, JwtProvider jwtProvider
    ) throws IOException {
        this.userRepository = userRepository;
        this.applePrivateKey = getPrivateKey(applePrivateKeyPath);
        this.mapper = mapper;
        this.client = client;
        this.jwtProvider = jwtProvider;
    }

    public static PrivateKey getPrivateKey(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        String privateKey = new String(Files.readAllBytes(path));
        Reader pemReader = new StringReader(privateKey);
        var pemParser = new PEMParser(pemReader);
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
        return converter.getPrivateKey(object);
    }


    public TokenSet handleAppleLogin(String code) throws UnauthorizedException {
        RequestBody formBody = new FormBody.Builder()
                .add("client_id", "com.SideProject.Group")
                .add("code", code)
                .add("grant_type", "authorization_code")
                .add("client_secret", createJWTAppleClientSecret())
                .build();

        var request = new Request.Builder().
                url(APPLE_TOKEN_URL).
                post(formBody).
                header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE).build();

        try (Response response = client.newCall(request).execute()) {
            var res = response.body();
            assert res != null;
            var resBody = res.string();
            System.out.println(resBody);

            if (!response.isSuccessful()) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, resBody);
            }
            var body = mapper.readValue(resBody, AppleGetTokenResponse.class);
            System.out.println(body);
            var decodedTokenClaims = parseJwtWithoutValidation(body.getIdToken());

            if(decodedTokenClaims == null) {
                throw new UnauthorizedException();
            }

            String socialId = (String) decodedTokenClaims.get("sub");
            var socialProvider = User.SocialProvider.APPLE;
            String refreshToken = body.getRefreshToken();

            User registeredUser = userRepository.findBySocialProviderAndSocialProviderId(socialProvider, socialId)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setSocialProvider(socialProvider);
                        newUser.setSocialProviderId(socialId);
                        newUser.setAppleRefreshToken(refreshToken);
                        return userRepository.save(newUser);
                    });

            return getTokenSet(registeredUser.getId());
        } catch (HttpClientErrorException | IOException e) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }




    public TokenSet getTokenSet(Long userId) {
        return new TokenSet(getUserAccessToken(userId, ACCESS_TOKEN_EXPIRES_IN), getUserRefreshToken(userId));
    }

    public String getUserRefreshToken(Long userId) {
        Map<String, Object> claims = new DefaultClaims();
        claims.put("type", "refresh");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRES_IN))
                .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes())
                .compact();
    }

    public String getUserAccessToken(Long userId, long expiresIn) {
        Map<String, Object> claims = new DefaultClaims();
        claims.put("type", "access");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiresIn))
                .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes())
                .compact();
    }

    private String createJWTAppleClientSecret() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("iss", appleTeamId);
        payload.put("aud", "https://appleid.apple.com");
        payload.put("sub", "com.SideProject.Group");

        return Jwts.builder()
                .setClaims(payload)
                .setHeaderParam(JwsHeader.ALGORITHM, "ES256")
                .setHeaderParam(JwsHeader.KEY_ID, appleKeyId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 180L * 24 * 60 * 60 * 1000))
                .signWith(SignatureAlgorithm.ES256, applePrivateKey)
                .compact();
    }

    public String reissueAccessToken(String accessToken, String refreshToken) throws BadRequestException, UnauthorizedException {
        if(!jwtProvider.isAccessToken(accessToken)){
            throw new BadRequestException("올바른 엑세스 토큰이 아닙니다.");
        }

        if(!jwtProvider.isExpired(accessToken)){
            throw new BadRequestException("토큰이 만료되지 않았습니다.");
        }

        if(!jwtProvider.isRefreshToken(refreshToken)){
            throw new BadRequestException("리프레시 토큰이 아닙니다.");
        }

        var accessTokenUserId = jwtProvider.getUserId(accessToken, true);
        var refreshTokenUserId = jwtProvider.getUserId(refreshToken);

        if(!accessTokenUserId.equals(refreshTokenUserId)){
            throw new UnauthorizedException();
        }

        return getUserAccessToken(accessTokenUserId, ACCESS_TOKEN_EXPIRES_IN);
    }

    public String testToken(Long userId, int expiresIn){
        return getUserAccessToken(userId, expiresIn);
    }
}

// Apple login response DTO
@Getter
@Setter
class AppleGetTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("id_token")
    private String idToken;
}
