package com.sideprj.groupmeeting.dto.meeting;

import java.time.LocalDateTime;

public record GetMeetingPlanCommentReport(
        Long id,
        String reasons,
        Long reporterId,
        String reporterNickname,
        String reporterProfileImgUrl,
        Long subjectId,
        String subjectNickname,
        String subjectProfileImgUrl,
        String originalContents,

        LocalDateTime createdAt
) {
}
