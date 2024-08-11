package com.sideprj.groupmeeting.mapper;

import com.sideprj.groupmeeting.dto.meeting.GetMeetingDto;
import com.sideprj.groupmeeting.dto.meeting.GetMeetingInviteDto;
import com.sideprj.groupmeeting.dto.meeting.GetMeetingMemberDto;
import com.sideprj.groupmeeting.dto.meeting.GetMeetingPlanDto;
import com.sideprj.groupmeeting.dto.user.GetUserDto;
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

    @Named("getInviteUrl")
    static String getInviteUrl(UUID value) {
        var url = "https://deeplink.ugsm.co.kr";
        return "%s/m/%s".formatted(url, value.toString());
    }

    @Named("getUserId")
    static Long getUserId(User user) {
        return user.getId();
    }

    @Named("getUserName")
    static String getUserName(User user) {
        return user.getNickname();
    }

    @Named("meetingToDto")
    @Mapping(source = "creator", target = "creatorId", qualifiedByName = "getUserId")
    @Mapping(source = "creator", target = "creatorName", qualifiedByName = "getUserName")
    GetMeetingDto toGetDto(Meeting meeting);

    @Named("planToDto")
    GetMeetingPlanDto toGetPlanDto(MeetingPlan meetingPlan);

    @IterableMapping(qualifiedByName = "planToDto")
    List<GetMeetingPlanDto> toGetPlanDtos(List<MeetingPlan> meetingPlan);


    @Named("memberToDto")
    @Mapping(source = "user", target = "userId", qualifiedByName = "getUserId")
    @Mapping(source = "user", target = "username", qualifiedByName = "getUserName")
    @Mapping(source = "createdAt", target = "joinedAt")
    GetMeetingMemberDto toGetMemberDto(MeetingMember meetingMember);

    @IterableMapping(qualifiedByName = "memberToDto")
    List<GetMeetingMemberDto> toGetMembersDto(List<MeetingMember> meetingMember);

    @Mapping(source = "id", target = "inviteUrl", qualifiedByName = "getInviteUrl")
    GetMeetingInviteDto toGetInviteDto(MeetingInvite meetingInvite);

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.nickname", target = "nickname")
    @Mapping(source = "user.profileImgUrl", target = "profileImgUrl")
    GetUserDto planParticiapantToGetUserDto(MeetingPlanParticipant meetingParticipant);

    @IterableMapping(qualifiedByName = "meetingToDto")
    List<GetMeetingDto> toGetDtos(List<Meeting> meetings);
}
