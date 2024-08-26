package com.sideprj.groupmeeting.dto.meeting;

import java.time.LocalDateTime;

public record GetMeetingPlanCommentDto(
        Long id,
        Long creatorId,
        String creatorNickname,
        String creatorProfileImgUrl,

        String contents,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}
