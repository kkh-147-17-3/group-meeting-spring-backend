package com.sideprj.groupmeeting.dto.user;

import com.sideprj.groupmeeting.entity.User;

public record GetUserDto(Long id, String nickname, String profileImgUrl) {

    public static GetUserDto fromEntity(User entity) {
        var profileImgUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", "meeting-sideproject", "ap-northeast-2",entity.getProfileImgName());
        return new GetUserDto(entity.getId(), entity.getNickname(), profileImgUrl);
    }
}