package com.sideprj.groupmeeting.dto.meeting;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

public record UpdateMeetingDto(@NonNull Long meetingId, String name, MultipartFile image) {}
