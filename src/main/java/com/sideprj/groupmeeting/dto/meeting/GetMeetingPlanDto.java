package com.sideprj.groupmeeting.dto.meeting;

import com.sideprj.groupmeeting.entity.meeting.MeetingPlan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GetMeetingPlanDto(
        Long id,
        String name,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String address,
        String detailAddress,
        BigDecimal longitude,
        BigDecimal latitude,
        LocalDateTime createdAt
) {
    public static GetMeetingPlanDto fromEntity(MeetingPlan entity) {
        return new GetMeetingPlanDto(
                entity.getId(),
                entity.getName(),
                entity.getStartAt(),
                entity.getEndAt(),
                entity.getAddress(),
                entity.getDetailAddress(),
                entity.getLongitude(),
                entity.getLatitude(),
                entity.getCreatedAt()
        );
    }
}
