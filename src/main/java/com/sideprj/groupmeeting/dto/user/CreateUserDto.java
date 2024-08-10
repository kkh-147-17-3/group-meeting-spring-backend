package com.sideprj.groupmeeting.dto.user;

import com.sideprj.groupmeeting.entity.User;
import lombok.Getter;

public record CreateUserDto(User.SocialProvider socialProvider, String socialProviderId){}
