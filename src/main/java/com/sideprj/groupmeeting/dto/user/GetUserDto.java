package com.sideprj.groupmeeting.dto.user;

public record GetUserDto(Long id, String nickname, String profileImgUrl, int badgeCount) {
}