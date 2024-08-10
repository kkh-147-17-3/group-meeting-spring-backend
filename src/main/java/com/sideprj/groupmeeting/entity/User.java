package com.sideprj.groupmeeting.entity;

import jakarta.persistence.*;
import lombok.*;

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
    private String appleRefreshToken;

    public enum SocialProvider {
        KAKAO, APPLE
    }

    public String getProfileImgUrl(){
        return String.format("https://%s.s3.%s.amazonaws.com/%s", "meeting-sideproject", "ap-northeast-2", this.getProfileImgName());
    }
}