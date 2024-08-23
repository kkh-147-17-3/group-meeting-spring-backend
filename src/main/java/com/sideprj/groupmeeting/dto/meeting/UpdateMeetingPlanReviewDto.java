package com.sideprj.groupmeeting.dto.meeting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class UpdateMeetingPlanReviewDto {
    private Long meetingPlanId;
    private String contents;
    private Long creatorId;
    private List<Long> deletedImageIds;
    private MultipartFile[] updatedImages;
}

