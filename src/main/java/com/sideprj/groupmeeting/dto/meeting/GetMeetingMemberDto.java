package com.sideprj.groupmeeting.dto.meeting;

import java.time.LocalDateTime;

public record GetMeetingMemberDto(Long id, Long userId, String username, LocalDateTime joinedAt) {}
