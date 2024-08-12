package com.sideprj.groupmeeting.dto.meeting;

import java.time.LocalDateTime;

public record GetMeetingMemberDto(Long id, Long userId, String userNickname, LocalDateTime joinedAt) {}
