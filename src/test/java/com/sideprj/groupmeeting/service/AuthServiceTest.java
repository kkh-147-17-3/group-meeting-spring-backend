package com.sideprj.groupmeeting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sideprj.groupmeeting.dto.TokenSet;
import com.sideprj.groupmeeting.entity.User;
import com.sideprj.groupmeeting.exceptions.BadRequestException;
import com.sideprj.groupmeeting.exceptions.UnauthorizedException;
import com.sideprj.groupmeeting.jwt.JwtProvider;
import com.sideprj.groupmeeting.repository.UserRepository;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OkHttpClient okHttpClient;

    @Mock
    private Call call;

    @Mock
    private Response response;

    @Mock
    private ResponseBody responseBody;

    @Mock
    private JwtProvider jwtProvider;

    private ECPrivateKey ecPrivateKey;

    @BeforeEach
    void setUp() throws IOException, NoSuchAlgorithmException {
        MockitoAnnotations.openMocks(this);
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        keyGen.initialize(256);
        KeyPair pair = keyGen.generateKeyPair();
        ecPrivateKey = (ECPrivateKey) pair.getPrivate();

        try (var mockedStatic = mockStatic(AuthService.class)) {
            mockedStatic.when(() -> AuthService.getPrivateKey(anyString())).thenReturn(mock(PrivateKey.class));

            authService = new AuthService(userRepository, objectMapper, okHttpClient, "dummy/path", jwtProvider);
        }
        ReflectionTestUtils.setField(authService, "jwtSecret", "testSecret");
        ReflectionTestUtils.setField(authService, "appleTeamId", "testTeamId");
        ReflectionTestUtils.setField(authService, "appleKeyId", "testKeyId");
        ReflectionTestUtils.setField(authService, "applePrivateKey", ecPrivateKey);
    }

    @Test
    void handleAppleLogin_Success() throws Exception {
        String code = "testCode";
        String idToken = "header.eyJzdWIiOiJ0ZXN0U3ViIn0.signature";
        String refreshToken = "testRefreshToken";

        var decodedTokenClaims = new HashMap<String, Object>();
        var socialProvider = User.SocialProvider.APPLE;
        var socialProviderId = "000000.9bd598d0000441bfb67be2b66279eff0.0000";
        decodedTokenClaims.put("sub", socialProviderId);

        AppleGetTokenResponse tokenResponse = new AppleGetTokenResponse();
        tokenResponse.setIdToken(idToken);
        tokenResponse.setRefreshToken(refreshToken);

        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn("dummy response");
        when(objectMapper.readValue(anyString(), eq(AppleGetTokenResponse.class))).thenReturn(tokenResponse);

        try (MockedStatic<JwtProvider> jwtProviderMock = mockStatic(JwtProvider.class)) {
            jwtProviderMock.when(() -> JwtProvider.parseJwtWithoutValidation(eq(idToken))).thenReturn(decodedTokenClaims);

            User user = new User();
            user.setId(1L);
            when(userRepository.findBySocialProviderAndSocialProviderId(eq(socialProvider), eq(socialProviderId))).thenReturn(Optional.of(user));

            // Act
            TokenSet result = authService.handleAppleLogin(code);

            // Assert
            assertNotNull(result);
            assertNotNull(result.accessToken());
            assertNotNull(result.refreshToken());

            // Verify JWT parsing was called
            jwtProviderMock.verify(() -> JwtProvider.parseJwtWithoutValidation(eq(idToken)));
        }

        // Verify other method calls
        verify(userRepository).findBySocialProviderAndSocialProviderId(eq(socialProvider), eq(socialProviderId));
    }

    @Test
    void handleAppleLogin_NewUser() throws Exception {
        // Arrange
        String code = "testCode";
        String idToken = "header.eyJzdWIiOiJ0ZXN0U3ViIn0.signature";
        String refreshToken = "testRefreshToken";

        AppleGetTokenResponse tokenResponse = new AppleGetTokenResponse();
        tokenResponse.setIdToken(idToken);
        tokenResponse.setRefreshToken(refreshToken);

        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn("dummy response");
        when(objectMapper.readValue(anyString(), eq(AppleGetTokenResponse.class))).thenReturn(tokenResponse);

        when(userRepository.findBySocialProviderAndSocialProviderId(any(), anyString())).thenReturn(Optional.empty());
        User newUser = new User();
        newUser.setId(1L);
        when(userRepository.save(any())).thenReturn(newUser);

        // Act
        TokenSet result = authService.handleAppleLogin(code);

        // Assert
        assertNotNull(result);
        assertNotNull(result.accessToken());
        assertNotNull(result.refreshToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void handleAppleLogin_UnauthorizedException() throws Exception {
        // Arrange
        String code = "testCode";
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.isSuccessful()).thenReturn(false);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn("Unauthorized");

        // Act & Assert
        assertThrows(HttpClientErrorException.class, () -> authService.handleAppleLogin(code));
    }

    @Test
    void getTokenSet_Success() {
        // Arrange
        Long userId = 1L;

        // Act
        TokenSet result = authService.getTokenSet(userId);

        // Assert
        assertNotNull(result);
        assertNotNull(result.accessToken());
        assertNotNull(result.refreshToken());
    }

    @Test
    void reissueAccessToken_Success() throws UnauthorizedException, BadRequestException {
        // Arrange
        Long userId = 1L;
        String accessToken = "access token";
        String refreshToken = "refresh token";

        when(jwtProvider.isAccessToken(eq(accessToken))).thenReturn(true);
        when(jwtProvider.isExpired(eq(accessToken))).thenReturn(true);
        when(jwtProvider.isRefreshToken(eq(refreshToken))).thenReturn(true);
        when(jwtProvider.getUserId(eq(accessToken), eq(true))).thenReturn(userId);
        when(jwtProvider.getUserId(eq(refreshToken))).thenReturn(userId);

        // Act
        String result = authService.reissueAccessToken(accessToken, refreshToken);

        // Assert
        assertNotNull(result);
    }

    @Test
    void reissueAccessToken_NotValidAccessToken() {
        // Arrange
        String accessToken = "not valid access token";
        String refreshToken = "refresh token";

        when(jwtProvider.isAccessToken(eq(accessToken))).thenReturn(false);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> authService.reissueAccessToken(accessToken, refreshToken));
    }

    @Test
    void reissueAccessToken_NotExpiredAccessToken() {
        // Arrange
        String accessToken = "not expired access token";
        String refreshToken = "refresh token";

        when(jwtProvider.isExpired(eq(accessToken))).thenReturn(false);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> authService.reissueAccessToken(accessToken, refreshToken));
    }

    @Test
    void reissueAccessToken_NotRefreshToken() {
        String accessToken = "not expired access token";
        String refreshToken = "not valid refresh token";

        when(jwtProvider.isExpired(eq(accessToken))).thenReturn(true);
        when(jwtProvider.isRefreshToken(eq(refreshToken))).thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> authService.reissueAccessToken(accessToken, refreshToken));
    }

    @Test
    void reissueAccessToken_MismatchedSubjectOfAccessTokenAndRefreshToken() {
        // Arrange
        String accessToken = "access token";
        String refreshToken = "refresh token";

        when(jwtProvider.isAccessToken(eq(accessToken))).thenReturn(true);
        when(jwtProvider.isExpired(eq(accessToken))).thenReturn(true);
        when(jwtProvider.isRefreshToken(eq(refreshToken))).thenReturn(true);
        when(jwtProvider.getUserId(eq(accessToken), eq(true))).thenReturn(1L);
        when(jwtProvider.getUserId(eq(accessToken))).thenReturn(2L);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> authService.reissueAccessToken(accessToken, refreshToken));
    }
}