package com.sideprj.groupmeeting.mapper;

import com.sideprj.groupmeeting.dto.meeting.*;
import com.sideprj.groupmeeting.entity.User;
import com.sideprj.groupmeeting.entity.meeting.*;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MeetingMapper {

    MeetingMapper INSTANCE = Mappers.getMapper(MeetingMapper.class);

    @Named("getUserId")
    static Long getUserId(User user) {
        return user.getId();
    }

    @Named("getUserNickname")
    static String getUserNickname(User user) {
        return user.getNickname();
    }

    @Mapping(source = "meeting.id", target = "id")
    @Mapping(source = "meeting.name", target = "name")
    @Mapping(source = "meeting.createdAt", target = "createdAt")
    @Mapping(source = "meeting.creator", target = "creatorId", qualifiedByName = "getUserId")
    @Mapping(source = "meeting.creator", target = "creatorNickname", qualifiedByName = "getUserNickname")
    GetMeetingDetailDto toGetDetailDto(Meeting meeting, MeetingPlan latestActivePlan, MeetingPlan latestClosedPlan);

    @Named("meetingToDto")
    @Mapping(source = "creator", target = "creatorId", qualifiedByName = "getUserId")
    @Mapping(source = "creator", target = "creatorNickname", qualifiedByName = "getUserNickname")
    GetMeetingDto toGetDto(Meeting meeting);

    GetMeetingPlanDto toGetPlanDto(MeetingPlan meetingPlan);

    @Named("planWithoutCommentsToDto")
    @Mapping(target = "activeComments", ignore = true)
    GetMeetingPlanDto toGetPlanWithoutCommentsDto(MeetingPlan meetingPlan);


    @IterableMapping(qualifiedByName = "planWithoutCommentsToDto")
    List<GetMeetingPlanDto> toGetPlanDtos(List<MeetingPlan> meetingPlan);

    @Named("planWithMeetingInfoToDto")
    @Mapping(target = "activeComments", ignore = true)
    @Mapping(source = "meeting.id", target = "meetingId")
    @Mapping(source = "meeting.name", target = "meetingName")
    GetMeetingPlanWithMeetingInfoDto toGetPlanWithMeeitngInfoDto(MeetingPlan meetingPlan);

    @IterableMapping(qualifiedByName = "planWithMeetingInfoToDto")
    List<GetMeetingPlanWithMeetingInfoDto> toGetPlanWithMeeitngInfoDtos(List<MeetingPlan> meetingPlan);


    @Named("memberToDto")
    @Mapping(source = "user", target = "userId", qualifiedByName = "getUserId")
    @Mapping(source = "user", target = "userNickname", qualifiedByName = "getUserNickname")
    @Mapping(source = "createdAt", target = "joinedAt")
    GetMeetingMemberDto toGetMemberDto(MeetingMember meetingMember);

    @IterableMapping(qualifiedByName = "memberToDto")
    List<GetMeetingMemberDto> toGetMembersDto(List<MeetingMember> meetingMember);

    GetMeetingInviteDto toGetInviteDto(MeetingInvite meetingInvite);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.nickname", target = "userNickname")
    @Mapping(source = "user.profileImgUrl", target = "userProfileImgUrl")
    GetMeetingPlanParticipantDto planParticipantToGetUserDto(MeetingPlanParticipant meetingParticipant);

    @IterableMapping(qualifiedByName = "meetingToDto")
    List<GetMeetingDto> toGetDtos(List<Meeting> meetings);


    @Mapping(source = "creator.id", target = "creatorId")
    @Mapping(source = "creator.nickname", target = "creatorNickname")
    @Mapping(source = "creator.profileImgUrl", target = "creatorProfileImgUrl")
    GetMeetingPlanCommentDto toGetPlanCommentDto(MeetingPlanComment comment);
}
