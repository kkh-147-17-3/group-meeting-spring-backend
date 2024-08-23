package com.sideprj.groupmeeting.dto.meeting;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class CreateMeetingPlanReviewDto {
    private Long meetingPlanId;
    private String contents;
    private Long creatorId;
    private MultipartFile[] imgFiles;
}
