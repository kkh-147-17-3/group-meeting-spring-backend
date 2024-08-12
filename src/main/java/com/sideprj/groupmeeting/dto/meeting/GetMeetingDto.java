package com.sideprj.groupmeeting.dto.meeting;

import com.sideprj.groupmeeting.entity.meeting.Meeting;

import java.time.LocalDateTime;
import java.util.List;

public record GetMeetingDto(
        Long id,
        String name,
        Long creatorId,
        String creatorName,
        String imageUrl,
        List<GetMeetingMemberDto> members,
        LocalDateTime createdAt
) {}
