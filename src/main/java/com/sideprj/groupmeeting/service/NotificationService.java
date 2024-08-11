package com.sideprj.groupmeeting.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sideprj.groupmeeting.dto.NotificationRequest;
import com.sideprj.groupmeeting.dto.NotificationRequestResult;
import com.sideprj.groupmeeting.exceptions.ResourceNotFoundException;
import com.sideprj.groupmeeting.exceptions.UnauthorizedException;
import com.sideprj.groupmeeting.repository.NotificationRepository;
import com.sideprj.groupmeeting.util.AppleJwtTokenUtil;
import lombok.SneakyThrows;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class NotificationService {

    private static final String APNS_URL = "https://api.push.apple.com/3/device/";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final NotificationRepository notificationRepository;

    private final OkHttpClient client;

    private final ObjectMapper mapper;

    private final PrivateKey applePrivateKey;

    public NotificationService(
            NotificationRepository notificationRepository,
            OkHttpClient client,
            ObjectMapper mapper,
            @Value("${apple.private_key_path}") String applePrivateKeyPath
    ) throws IOException {
        this.notificationRepository = notificationRepository;
        this.client = client;
        this.mapper = mapper;
        this.applePrivateKey = AuthService.getPrivateKey(applePrivateKeyPath);
    }

    @SneakyThrows
    public String buildPayload(
            String title,
            String body,
            Integer badge,
            String sound,
            Map<String, Object> customData
    ) {
        // Create the APS payload
        Map<String, Object> aps = new HashMap<>();
        Map<String, String> alert = new HashMap<>();
        alert.put("title", title);
        alert.put("body", body);
        aps.put("alert", alert);
        aps.put("badge", badge);
        aps.put("sound", sound);

        // Create the full payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("aps", aps);
        if (customData != null) {
            payload.putAll(customData);
        }

        return mapper.writeValueAsString(payload);
    }

    @Transactional
    public void updateAsRead(long userId, long notificationId) throws ResourceNotFoundException, UnauthorizedException {
        var notification = notificationRepository.findById(notificationId).orElseThrow(ResourceNotFoundException::new);

        if (!notification.getUser().getId().equals(userId)) throw new UnauthorizedException();
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @SneakyThrows
    public CompletableFuture<NotificationRequestResult> sendPushNotification(
            NotificationRequest request,
            String jwtToken
    ) {
        String url = APNS_URL + request.deviceToken();
        TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {};
        var data = request.data() == null ? null : mapper.readValue(request.data(), typeReference);
        var requestData = buildPayload(request.title(), request.message(), request.badgeCount(), "default", data);
        RequestBody body = RequestBody.create(requestData, JSON);

        Request httpRequest = new Request.Builder()
                .url(url)
                .addHeader("apns-topic", "com.SideProject.Group")
                .addHeader("authorization", "bearer " + jwtToken)
                .post(body)
                .build();

        CompletableFuture<NotificationRequestResult> future = new CompletableFuture<>();

        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    future.complete(new NotificationRequestResult(
                            request.id(),
                            false,
                            request.title(),
                            request.deviceToken(),
                            request.deviceType(),
                            request.message(),
                            requestData,
                            response.body() != null ? response.body().string() : null
                    ));
                } else {
                    future.complete(new NotificationRequestResult(
                            request.id(),
                            true,
                            request.title(),
                            request.deviceToken(),
                            request.deviceType(),
                            request.message(),
                            requestData,
                            null
                    ));
                }
            }
        });

        return future;
    }

    public List<CompletableFuture<NotificationRequestResult>> sendMultipleNotifications(List<NotificationRequest> requests) throws Exception {
        String jwtToken = AppleJwtTokenUtil.generateToken();

        // Send notifications asynchronously to all device tokens
        return requests.stream()
                       .map((request) -> sendPushNotification(request, jwtToken))
                       .toList();
    }
}
