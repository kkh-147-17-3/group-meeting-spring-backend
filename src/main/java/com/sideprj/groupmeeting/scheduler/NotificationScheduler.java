package com.sideprj.groupmeeting.scheduler;

import com.sideprj.groupmeeting.dto.NotificationRequest;
import com.sideprj.groupmeeting.entity.Notification;
import com.sideprj.groupmeeting.repository.MeetingRepositorySupport;
import com.sideprj.groupmeeting.repository.NotificationRepository;
import com.sideprj.groupmeeting.repository.UserRepository;
import com.sideprj.groupmeeting.service.NotificationService;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MeetingRepositorySupport meetingRepositorySupport;

    private final OkHttpClient okHttpClient;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void sendNotification() throws Exception {
        var notifications = notificationRepository.findByScheduledAtBeforeAndSentAtNull(LocalDateTime.now());
        var requests = notifications.stream()
                                    .map(noti -> new NotificationRequest(
                                            noti.getId(),
                                            noti.getTitle(),
                                            noti.getMessage(),
                                            noti.getActionData(),
                                            noti.getActionType(),
                                            noti.getDeviceToken(),
                                            noti.getDeviceType(),
                                            noti.getUser().getBadgeCount() + 1
                                    ))
                                    .toList();

        var futures = notificationService.sendMultipleNotifications(requests);


        var allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.get();
        List<Long> successfulNotificationIds = new ArrayList<>();
        for (var future : futures) {
            var result = future.get();
            if (result.isSuccessful()) {
                successfulNotificationIds.add(result.id());
            } else {
                System.err.println("Failed for device token " + "result: " + result);
            }
        }

        var successfulNotifications = notificationRepository.findAllById(successfulNotificationIds);
        var users = successfulNotifications.stream().map(Notification::getUser).toList();
        users.forEach(user -> user.setBadgeCount(user.getBadgeCount() + 1));
        successfulNotifications.forEach(notification -> notification.setSentAt(LocalDateTime.now()));
        notificationRepository.saveAll(successfulNotifications);
        userRepository.saveAll(users);
    }


    @Scheduled(fixedDelay = 10 * 60 * 1000)
    @Transactional
    public void sendToMeetingCreatorWhereNoPlansAreCreated() {
        var meetings = meetingRepositorySupport.findWhereNoPlansFound();
        var title = "모임 약속을 추가해주세요.";
        var message = "모임을 만드신지 한 시간이 지났습니다. 약속을 추가해보시는 것은 어떨까요?";
        var notifications = meetings
                .stream()
                .map(meeting -> Notification.builder()
                                            .deviceToken(meeting.getCreator()
                                                                .getDeviceToken())
                                            .deviceType(meeting.getCreator()
                                                               .getDeviceType())
                                            .scheduledAt(meeting.getCreatedAt()
                                                                .plusHours(1))
                                            .title(title)
                                            .message(message)
                                            .build()
                ).toList();
        notificationRepository.saveAll(notifications);
    }
}
