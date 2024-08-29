package com.sideprj.groupmeeting.dto.meeting;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetMeetingMemberDto {
    private Long id;
    private Long userId;
    private String userNickname;
    private LocalDateTime joinedAt;
}
