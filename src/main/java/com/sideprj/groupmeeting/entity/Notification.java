package com.sideprj.groupmeeting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column
    private User.DeviceType deviceType;

    @Column
    private String deviceToken;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    @Column
    private LocalDateTime readAt;

    @Enumerated(EnumType.STRING)
    @Column
    @Builder.Default
    private ActionType actionType = ActionType.DEFAULT;

    @Column
    private String actionData;

    @Column
    private LocalDateTime sentAt;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Column
    @Builder.Default
    private LocalDateTime expiredAt = LocalDateTime.parse("2999-12-31T00:00:00");


    public enum ActionType {
        DEFAULT
    }
}