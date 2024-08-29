package com.sideprj.groupmeeting.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sideprj.groupmeeting.dto.NotificationBody;
import com.sideprj.groupmeeting.dto.NotificationRequest;
import com.sideprj.groupmeeting.entity.Notification;
import com.sideprj.groupmeeting.repository.NotificationRepository;
import com.sideprj.groupmeeting.repository.NotificationRepositorySupport;
import com.sideprj.groupmeeting.repository.UserRepository;
import com.sideprj.groupmeeting.repository.meeting.MeetingPlanRepository;
import com.sideprj.groupmeeting.repository.meeting.MeetingRepositorySupport;
import com.sideprj.groupmeeting.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationRepository notificationRepository;
    private final NotificationRepositorySupport notificationRepositorySupport;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MeetingRepositorySupport meetingRepositorySupport;
    private final MeetingPlanRepository meetingPlanRepository;

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void sendNotification() throws Exception {
        var notifications = notificationRepositorySupport.findAllNeedToBeSentFromNow();
        var requests = notifications.stream()
                .map(noti -> {
                    noti.getDataBody().setNotificationId(noti.getId());
                    return new NotificationRequest(
                            noti.getId(),
                            noti.getTitle(),
                            noti.getMessage(),
                            noti.getDataBody().getData(),
                            noti.getActionType(),
                            noti.getDeviceToken(),
                            noti.getDeviceType(),
                            noti.getUser().getBadgeCount() + 1
                    );
                })
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
                .map(meeting -> {
                            Map<String, Object> data = Map.of(
                                    "meetingId", meeting.getId()
                            );
                            var notificationBody = new NotificationBody(Notification.ActionType.MEETING_PLAN, data);

                            return Notification.builder()
                                    .deviceToken(meeting.getCreator()
                                            .getDeviceToken())
                                    .actionType(Notification.ActionType.MEETING)
                                    .deviceType(meeting.getCreator()
                                            .getDeviceType())
                                    .user(meeting.getCreator())
                                    .scheduledAt(meeting.getCreatedAt()
                                            .plusHours(1))
                                    .title(title)
                                    .message(message)
                                    .dataBody(notificationBody)
                                    .build();
                        }
                ).toList();
        notificationRepository.saveAll(notifications);
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    @Transactional
    public void sendToMeetingPlanParticipantBefore24Hours() {
        var minStartAt = LocalDateTime.now().plusDays(1);
        var maxStartAt = minStartAt.plusMinutes(10);
        var meetingPlans = meetingPlanRepository.findByStartAtBetween(minStartAt, maxStartAt);
        var title = "모임 약속 24시간 전 알림";
        var message = "%s 모임 약속까지 24시간 남았습니다. 약속 일정을 확인해주세요.";
        var notifications = new ArrayList<Notification>();
        meetingPlans.forEach(plan -> {
            var participants = plan.getParticipants();
            var notificationsPerPlan = participants
                    .stream()
                    .map((participant) -> {
                        Map<String, Object> data = Map.of(
                                "meetingPlanId", participant.getMeetingPlan().getId()
                        );
                        var notificationBody = new NotificationBody(Notification.ActionType.MEETING_PLAN, data);
                        return Notification.builder()
                                .user(participant.getUser())
                                .actionType(Notification.ActionType.MEETING_PLAN)
                                .deviceToken(participant.getUser().getDeviceToken())
                                .deviceType(participant.getUser().getDeviceType())
                                .scheduledAt(plan.getStartAt().minusDays(1))
                                .title(title)
                                .dataBody(notificationBody)
                                .message(message.formatted(plan.getName()))
                                .build();
                    })
                    .toList();
            notifications.addAll(notificationsPerPlan);
        });
        notificationRepository.saveAll(notifications);
    }
}
