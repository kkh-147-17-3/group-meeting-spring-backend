package com.sideprj.groupmeeting.dto.meeting;

public record GetMeetingPlanParticipantDto(Long id, Long userId, String userNickname, String userProfileImgUrl) {
}
