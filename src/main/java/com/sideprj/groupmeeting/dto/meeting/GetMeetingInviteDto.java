package com.sideprj.groupmeeting.dto.meeting;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetMeetingInviteDto(LocalDateTime expiredAt, String inviteUrl) {
}
