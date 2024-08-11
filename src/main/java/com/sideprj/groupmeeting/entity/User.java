package com.sideprj.groupmeeting.entity;

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

    public enum SocialProvider {
        KAKAO, APPLE
    }

    public enum DeviceType {
        IOS, ANDROID
    }

    public String getProfileImgUrl(){
        return String.format("https://%s.s3.%s.amazonaws.com/%s", "meeting-sideproject", "ap-northeast-2", this.getProfileImgName());
    }
}