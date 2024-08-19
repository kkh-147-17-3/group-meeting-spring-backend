package com.sideprj.groupmeeting.dto.meeting;

import java.time.LocalDateTime;
import java.util.List;

public record GetMeetingDetailDto(
        Long id,
        String name,
        Long creatorId,
        String creatorNickname,
        String imageUrl,
        List<GetMeetingMemberDto> members,
        LocalDateTime createdAt,
        MeetingPlanDto latestActivePlan,
        MeetingPlanDto latestClosedPlan
) {
    public record MeetingPlanDto(
       Long id,
       String name,
       LocalDateTime startAt,
       LocalDateTime endAt,
       String address,
       String detailAddress,
       Float temperature,
       String weatherIconUrl
    ){}
}
