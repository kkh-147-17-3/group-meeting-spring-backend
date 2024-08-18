package com.sideprj.groupmeeting.dto.meeting;

import java.time.LocalDateTime;

public record GetMeetingPlanCommentDto(
        Long id,
        Long creatorId,
        String creatorNickname,
        String creatorProfileImgUrl,

        String content,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}
