package com.sideprj.groupmeeting.entity;

import com.sideprj.groupmeeting.entity.meeting.Meeting;
import com.sideprj.groupmeeting.entity.meeting.MeetingPlan;
import com.sideprj.groupmeeting.entity.meeting.MeetingPlanParticipant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "`user`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialProvider socialProvider;

    @Column(nullable = false)
    private String socialProviderId;

    @Column
    private String nickname;

    @Column
    private String profileImgName;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column
    private String deviceToken;

    @Column
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    @Column
    private String appleRefreshToken;

    @Column
    private LocalDateTime lastLaunchAt;

    @Column(nullable = false, columnDefinition = "int default 0")
    @Builder.Default
    private int badgeCount = 0;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notifications;

    @OneToMany(mappedBy = "user")
    private List<MeetingPlanParticipant> meetingPlanParticipants;

    public enum SocialProvider {
        KAKAO, APPLE
    }

    public enum DeviceType {
        IOS, ANDROID
    }

    public String getProfileImgUrl() {
        return getProfileImgSource(this.getProfileImgName());
    }

    public static String getProfileImgSource(String key) {
        if (key == null) return null;
        return String.format("https://%s.s3.%s.amazonaws.com/%s", "meeting-sideproject", "ap-northeast-2", key);
    }

    public boolean hasJoinedMeetingPlan(MeetingPlan meetingPlan) {
        if (meetingPlan == null) {
            throw new NullPointerException();
        }

        return this.meetingPlanParticipants.stream()
                .anyMatch(participant -> participant.getMeetingPlan().getId().equals(meetingPlan.getId()));
    }

    public boolean hasJoinedMeeting(Meeting meeting) {
        if (meeting == null) {
            throw new NullPointerException();
        }

        return meeting.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(id));
    }

    public boolean isAbleToJoinMeetingPlan(MeetingPlan meetingPlan) {
        if (meetingPlan == null) {
            throw new NullPointerException();
        }

        return this.getMeetingPlanParticipants().stream()
                .anyMatch(participant -> {
                    var joinedMeetingPlanStartAt = participant.getMeetingPlan().getStartAt();
                    var joinedMeetingPlanEndAt = participant.getMeetingPlan().getEndAt();
                    joinedMeetingPlanEndAt = joinedMeetingPlanEndAt == null ? joinedMeetingPlanStartAt
                            .withHour(23)
                            .withMinute(59)
                            .withSecond(59) : joinedMeetingPlanEndAt;

                    return meetingPlan.getStartAt().isAfter(joinedMeetingPlanEndAt)
                            || joinedMeetingPlanStartAt.isAfter(meetingPlan.getEndAt());

                });
    }
}