package com.sideprj.groupmeeting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sideprj.groupmeeting.dto.NotificationRequest;
import com.sideprj.groupmeeting.dto.NotificationRequestResult;
import com.sideprj.groupmeeting.exceptions.ResourceNotFoundException;
import com.sideprj.groupmeeting.exceptions.UnauthorizedException;
import com.sideprj.groupmeeting.repository.NotificationRepository;
import com.sideprj.groupmeeting.repository.UserRepository;
import com.sideprj.groupmeeting.util.AppleJwtTokenUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class NotificationService {

    private static final String APNS_URL = "https://api.push.apple.com/3/device/";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private final OkHttpClient client;

    private final ObjectMapper mapper;

    private final PrivateKey applePrivateKey;

    public NotificationService(
            NotificationRepository notificationRepository, UserRepository userRepository,
            OkHttpClient client,
            ObjectMapper mapper,
            @Value("${apple.private_key_path}") String applePrivateKeyPath
    ) throws IOException {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
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

    @Async
    public CompletableFuture<NotificationRequestResult> sendPushNotification(
            NotificationRequest request,
            String jwtToken
    ) {
        CompletableFuture<NotificationRequestResult> future = new CompletableFuture<>();

        String url = APNS_URL + request.deviceToken();
        TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };
        Map<String, Object> data = null;
        try {
            if (request.data() != null) {
                data = mapper.readValue(request.data(), typeReference);
            }
        } catch (JsonProcessingException e) {
            future.complete(new NotificationRequestResult(
                    request.id(),
                    false,
                    request.title(),
                    request.deviceToken(),
                    request.deviceType(),
                    request.message(),
                    request.data(),
                    e.getMessage()
            ));
            return future;
        }


        var requestData = buildPayload(request.title(), request.message(), request.badgeCount(), "default", data);
        RequestBody body = RequestBody.create(requestData, JSON);

        Request httpRequest = new Request.Builder()
                .url(url)
                .addHeader("apns-topic", "com.SideProject.Group")
                .addHeader("authorization", "bearer " + jwtToken)
                .post(body)
                .build();

        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                log.error(e.getMessage());
                future.completeExceptionally(e);
                call.cancel();
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
                response.close();
            }
        });

        return future;
    }

    public List<CompletableFuture<NotificationRequestResult>> sendMultipleNotifications(List<NotificationRequest> requests) throws Exception {
        String jwtToken = AppleJwtTokenUtil.generateToken();

        var iterator = requests.iterator();
        var notificationPackage = new LinkedList<NotificationRequest>();
        var notificationResults = new LinkedList<CompletableFuture<NotificationRequestResult>>();
        while (iterator.hasNext()){
            var currRequest = (NotificationRequest) iterator.next();
            notificationPackage.add(currRequest);
            if(notificationPackage.size() == 200) {
                var results = requests.stream()
                        .map((request) -> sendPushNotification(request, jwtToken))
                        .toList();
                notificationResults.addAll(results);
                notificationPackage = new LinkedList<>();
            }
        }
        if (!notificationPackage.isEmpty()){
            var results = requests.stream()
                                  .map((request) -> sendPushNotification(request, jwtToken))
                                  .toList();
            notificationResults.addAll(results);
        }

        // Send notifications asynchronously to all device tokens
        return notificationResults;
    }

    @Transactional
    public int updateAllUnreadAsRead(Long userId) {
        var notifications = notificationRepository.findByUserIdAndSentAtIsNull(userId);
        if (notifications.isEmpty()) {
            return 0;
        }
        var user = notifications.get(0).getUser();
        user.setBadgeCount(0);
        notifications.forEach(noti -> noti.setReadAt(LocalDateTime.now()));
        userRepository.save(user);
        notificationRepository.saveAll(notifications);
        return notifications.size();
    }
}
