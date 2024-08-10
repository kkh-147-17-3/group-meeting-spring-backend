package com.sideprj.groupmeeting.dto.meeting;

import org.springframework.web.multipart.MultipartFile;

public record CreateMeetingDto(
        String name,
        MultipartFile image
) {}
