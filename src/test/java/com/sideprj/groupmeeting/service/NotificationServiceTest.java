package com.sideprj.groupmeeting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sideprj.groupmeeting.dto.NotificationRequest;
import com.sideprj.groupmeeting.dto.NotificationRequestResult;
import com.sideprj.groupmeeting.entity.Notification;
import com.sideprj.groupmeeting.entity.User;
import com.sideprj.groupmeeting.exceptions.ResourceNotFoundException;
import com.sideprj.groupmeeting.exceptions.UnauthorizedException;
import com.sideprj.groupmeeting.repository.NotificationRepository;
import com.sideprj.groupmeeting.util.AppleJwtTokenUtil;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private OkHttpClient client;

    @Mock
    private ObjectMapper mapper;

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

            notificationService = new NotificationService(notificationRepository, client,mapper, "dummy/path");
        }
        ReflectionTestUtils.setField(notificationService, "applePrivateKey", ecPrivateKey);
    }

    @Test
    void buildPayload_Success() throws Exception {
        // Arrange
        String title = "Test Title";
        String body = "Test Body";
        Integer badge = 1;
        String sound = "default";
        Map<String, Object> customData = new HashMap<>();
        customData.put("key", "value");

        Map<String, Object> expectedPayload = new HashMap<>();
        Map<String, Object> aps = new HashMap<>();
        Map<String, String> alert = new HashMap<>();
        alert.put("title", title);
        alert.put("body", body);
        aps.put("alert", alert);
        aps.put("badge", badge);
        aps.put("sound", sound);
        expectedPayload.put("aps", aps);
        expectedPayload.put("key", "value");

        when(mapper.writeValueAsString(any())).thenReturn("mocked_json_string");

        // Act
        String result = notificationService.buildPayload(title, body, badge, sound, customData);

        // Assert
        assertEquals("mocked_json_string", result);
        verify(mapper).writeValueAsString(expectedPayload);
    }

    @Test
    void updateAsRead_Success() throws ResourceNotFoundException, UnauthorizedException {
        // Arrange
        long userId = 1L;
        long notificationId = 1L;
        User user = new User();
        user.setId(userId);
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setUser(user);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // Act
        notificationService.updateAsRead(userId, notificationId);

        // Assert
        assertNotNull(notification.getReadAt());
        verify(notificationRepository).save(notification);
    }

    @Test
    void updateAsRead_NotFound() {
        // Arrange
        long userId = 1L;
        long notificationId = 1L;

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> notificationService.updateAsRead(userId, notificationId));
    }

    @Test
    void updateAsRead_Unauthorized() {
        // Arrange
        long userId = 1L;
        long notificationId = 1L;
        User user = new User();
        user.setId(2L); // Different user ID
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setUser(user);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> notificationService.updateAsRead(userId, notificationId));
    }

    @Test
    void sendPushNotification_Success() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest(1L, "Test Title", "Test Message", null, Notification.ActionType.DEFAULT, "testtoken", User.DeviceType.IOS, 0);
        String jwtToken = "dummy_jwt_token";

        when(mapper.writeValueAsString(any())).thenReturn("mocked_payload");

        Call call = mock(Call.class);
        when(client.newCall(any())).thenReturn(call);

        Response response = mock(Response.class);
        when(response.isSuccessful()).thenReturn(true);

        // Act
        CompletableFuture<NotificationRequestResult> future = notificationService.sendPushNotification(request, jwtToken);

        // Simulate OkHttp callback
        ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
        verify(call).enqueue(callbackCaptor.capture());
        Callback callback = callbackCaptor.getValue();
        callback.onResponse(call, response);

        // Assert
        NotificationRequestResult result = future.get();
        assertTrue(result.isSuccessful());
        assertEquals(request.id(), result.id());
        assertEquals(request.title(), result.title());
        assertEquals(request.deviceToken(), result.deviceToken());
        assertEquals(request.deviceType(), result.deviceType());
        assertEquals(request.message(), result.message());
    }

    @Test
    void sendMultipleNotifications_Success() throws Exception {
        // Arrange
        List<NotificationRequest> requests = List.of(
                new NotificationRequest(1L, "Title 1", "Message 1", null, Notification.ActionType.DEFAULT, "token1", User.DeviceType.IOS, 0),
                new NotificationRequest(2L, "Title 2", "Message 2", null, Notification.ActionType.DEFAULT, "token2", User.DeviceType.ANDROID, 0)
        );

        try (MockedStatic<AppleJwtTokenUtil> mockedStatic = mockStatic(AppleJwtTokenUtil.class)) {
            mockedStatic.when(AppleJwtTokenUtil::generateToken).thenReturn("dummy_jwt_token");

            when(mapper.writeValueAsString(any())).thenReturn("mocked_payload");

            Call call = mock(Call.class);
            when(client.newCall(any())).thenReturn(call);

            Response response = mock(Response.class);
            when(response.isSuccessful()).thenReturn(true);

            // Act
            List<CompletableFuture<NotificationRequestResult>> futures = notificationService.sendMultipleNotifications(requests);

            // Simulate OkHttp callbacks
            ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
            verify(call, times(2)).enqueue(callbackCaptor.capture());
            for (Callback callback : callbackCaptor.getAllValues()) {
                callback.onResponse(call, response);
            }

            // Assert
            assertEquals(2, futures.size());
            for (CompletableFuture<NotificationRequestResult> future : futures) {
                NotificationRequestResult result = future.get();
                assertTrue(result.isSuccessful());
            }
        }
    }

    @Test
    void sendPushNotification_Failure() throws Exception {
        // Arrange
        var request = new NotificationRequest(1L, "Title 1", "Message 1", null, Notification.ActionType.DEFAULT, "token1", User.DeviceType.IOS, 0);
        String jwtToken = "dummy_jwt_token";

        when(mapper.writeValueAsString(any())).thenReturn("mocked_payload");

        Call call = mock(Call.class);
        when(client.newCall(any())).thenReturn(call);

        // Act
        CompletableFuture<NotificationRequestResult> future = notificationService.sendPushNotification(request, jwtToken);

        // Simulate OkHttp callback failure
        ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
        verify(call).enqueue(callbackCaptor.capture());
        Callback callback = callbackCaptor.getValue();
        callback.onFailure(call, new IOException("Network error"));

        // Assert
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(IOException.class, exception.getCause());
        assertEquals("Network error", exception.getCause().getMessage());
    }

    @Test
    void sendPushNotification_ResponseNotSuccessful() throws Exception {
        // Arrange
        var request = new NotificationRequest(1L, "Title 1", "Message 1", null, Notification.ActionType.DEFAULT, "token1", User.DeviceType.IOS, 0);
        String jwtToken = "dummy_jwt_token";

        when(mapper.writeValueAsString(any())).thenReturn("mocked_payload");

        Call call = mock(Call.class);
        when(client.newCall(any())).thenReturn(call);

        Response response = mock(Response.class);
        when(response.isSuccessful()).thenReturn(false);
        ResponseBody responseBody = ResponseBody.create("{\"response\": \"invalid\"}", MediaType.get("application/json"));
        when(response.body()).thenReturn(responseBody);

        // Act
        CompletableFuture<NotificationRequestResult> future = notificationService.sendPushNotification(request, jwtToken);

        // Simulate OkHttp callback
        ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
        verify(call).enqueue(callbackCaptor.capture());
        Callback callback = callbackCaptor.getValue();
        callback.onResponse(call, response);

        // Assert
        NotificationRequestResult result = future.get();
        assertFalse(result.isSuccessful());
        assertEquals(request.id(), result.id());
        assertEquals(request.title(), result.title());
        assertEquals(request.deviceToken(), result.deviceToken());
        assertEquals(request.deviceType(), result.deviceType());
        assertEquals(request.message(), result.message());
        assertEquals("{\"response\": \"invalid\"}", result.responseBody());
    }
}