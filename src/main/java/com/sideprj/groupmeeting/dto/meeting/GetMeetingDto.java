package com.sideprj.groupmeeting.dto.meeting;

import com.sideprj.groupmeeting.entity.meeting.Meeting;

import java.time.LocalDateTime;
import java.util.List;

public record GetMeetingDto(
        Long id,
        String name,
        Long creatorId,
        String creatorName,
        String imageUrl,
//        List<GetMeetingPlanDto> plans,
        List<GetMeetingMemberDto> members,
        LocalDateTime createdAt
) {
//    public static GetMeetingDto fromEntity(Meeting meeting) {
//
//        var plans = meeting.getPlans()
//                           .stream()
//                           .map((plan) -> new GetMeetingPlanDto(
//                                   plan.getId(),
//                                   plan.getName(),
//                                   plan.getStartAt(),
//                                   plan.getEndAt(),
//                                   plan.getAddress(),
//                                   plan.getDetailAddress(),
//                                   plan.getLongitude(),
//                                   plan.getLatitude(),
//                                   plan.getStartAt()
//                           ))
//                           .toList();
//
//        var members = meeting.getMembers()
//                             .stream()
//                             .map(member -> new GetMeetingMemberDto(
//                                     member.getId(),
//                                     member.getUser()
//                                           .getId(),
//                                     member.getUser()
//                                           .getNickname(),
//                                     member.getCreatedAt()
//                             ))
//                             .toList();
//        var imageUrl = String.format(
//                "https://%s.s3.%s.amazonaws.com/%s",
//                "meeting-sideproject",
//                "ap-northeast-2",
//                meeting.getMainImageName()
//        );
//        return new GetMeetingDto(
//                meeting.getId(),
//                meeting.getName(),
//                meeting.getCreator()
//                       .getId(),
//                meeting.getCreator()
//                       .getNickname(),
//                imageUrl,
//                plans,
//                members,
//                meeting.getCreatedAt()
//        );
//    }
}
