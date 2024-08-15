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

    @Named("meetingToDto")
    @Mapping(source = "creator", target = "creatorId", qualifiedByName = "getUserId")
    @Mapping(source = "creator", target = "creatorNickname", qualifiedByName = "getUserNickname")
    GetMeetingDto toGetDto(Meeting meeting);

    @Named("planToDto")
    GetMeetingPlanDto toGetPlanDto(MeetingPlan meetingPlan);

    @IterableMapping(qualifiedByName = "planToDto")
    List<GetMeetingPlanDto> toGetPlanDtos(List<MeetingPlan> meetingPlan);


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
}
