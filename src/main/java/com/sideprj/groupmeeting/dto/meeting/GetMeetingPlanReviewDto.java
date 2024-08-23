package com.sideprj.groupmeeting.dto.meeting;

import java.util.List;

public record GetMeetingPlanReviewDto(
        Long id,
        String contents,

        List<GetMeetingPlanReviewImageDto> images
) {

    public record GetMeetingPlanReviewImageDto(
            String imageUrl
    ){}
}
