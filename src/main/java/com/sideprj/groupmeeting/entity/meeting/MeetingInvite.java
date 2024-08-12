package com.sideprj.groupmeeting.entity.meeting;

import com.sideprj.groupmeeting.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetingInvite extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private LocalDateTime expiredAt;

    @ManyToOne
    private Meeting meeting;

    public String getInviteUrl() {
        var url = "https://deeplink.ugsm.co.kr";
        return "%s/m/%s".formatted(url, id.toString());
    }
}
