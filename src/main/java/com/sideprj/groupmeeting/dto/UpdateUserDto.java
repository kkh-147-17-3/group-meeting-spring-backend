package com.sideprj.groupmeeting.dto;

import org.springframework.web.multipart.MultipartFile;

public record UpdateUserDto(MultipartFile profileImg, String nickname) {}
