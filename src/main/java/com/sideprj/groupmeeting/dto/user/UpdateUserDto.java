package com.sideprj.groupmeeting.dto.user;

import org.springframework.web.multipart.MultipartFile;

public record UpdateUserDto(MultipartFile profileImg, String nickname) {}
