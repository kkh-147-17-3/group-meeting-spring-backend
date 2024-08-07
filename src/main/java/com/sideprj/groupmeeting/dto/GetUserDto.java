package com.sideprj.groupmeeting.dto;

import com.sideprj.groupmeeting.entity.User;

public record GetUserDto(Long id, String nickname, String profileImgUrl) {

    public static GetUserDto fromEntity(User entity) {
        return new GetUserDto(
                entity.getId(),
                entity.getNickname(),
                entity.getProfileImgUrl()
        );
    }
}