package com.sideprj.groupmeeting.dto.meeting;

import java.util.List;

public record GetMeetingPlanReviewDto(
        Long id,
        String contents,

        Long creatorId,

        String creatorNickname,
        String creatorProfileImgUrl,

        List<GetMeetingPlanReviewImageDto> images
) {

    public record GetMeetingPlanReviewImageDto(
            Long id,
            String imageUrl
    ){}
}
