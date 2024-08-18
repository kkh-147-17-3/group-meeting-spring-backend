package com.sideprj.groupmeeting.dto.meeting;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record GetMeetingPlanWithMeetingInfoDto(
        Long id,
        String name,
        Long meetingId,
        String meetingName,
        List<GetMeetingPlanParticipantDto> participants,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String address,
        String detailAddress,
        BigDecimal longitude,
        BigDecimal latitude,
        LocalDateTime createdAt,
        List<GetMeetingPlanCommentDto> activeComments,
        Float temperature,
        String weatherIconUrl
){}