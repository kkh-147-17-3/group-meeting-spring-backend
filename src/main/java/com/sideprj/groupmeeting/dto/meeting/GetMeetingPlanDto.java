package com.sideprj.groupmeeting.dto.meeting;

import com.sideprj.groupmeeting.dto.user.GetUserDto;
import com.sideprj.groupmeeting.entity.meeting.MeetingPlan;
import com.sideprj.groupmeeting.entity.meeting.MeetingPlanParticipant;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record GetMeetingPlanDto(
        Long id,
        String name,
        List<GetMeetingPlanParticipantDto> participants,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String address,
        String detailAddress,
        BigDecimal longitude,
        BigDecimal latitude,
        LocalDateTime createdAt
){}