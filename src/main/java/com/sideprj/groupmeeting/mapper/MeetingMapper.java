package com.sideprj.groupmeeting.mapper;

import com.sideprj.groupmeeting.dto.meeting.GetMeetingDto;
import com.sideprj.groupmeeting.dto.meeting.GetMeetingInviteDto;
import com.sideprj.groupmeeting.dto.meeting.GetMeetingMemberDto;
import com.sideprj.groupmeeting.dto.meeting.GetMeetingPlanDto;
import com.sideprj.groupmeeting.entity.User;
import com.sideprj.groupmeeting.entity.meeting.Meeting;
import com.sideprj.groupmeeting.entity.meeting.MeetingInvite;
import com.sideprj.groupmeeting.entity.meeting.MeetingMember;
import com.sideprj.groupmeeting.entity.meeting.MeetingPlan;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

@Mapper
public interface MeetingMapper {
    MeetingMapper INSTANCE = Mappers.getMapper(MeetingMapper.class);

    @Named("meetingToDto")
    @Mapping(source = "creator", target = "creatorId", qualifiedByName = "getUserId")
    @Mapping(source = "creator", target = "creatorName", qualifiedByName = "getUserName")
    GetMeetingDto toGetDto(Meeting meeting);

    GetMeetingPlanDto toGetPlanDto(MeetingPlan meetingPlan);

    @Named("memberToDto")
    @Mapping(source = "user", target = "userId", qualifiedByName = "getUserId")
    @Mapping(source = "user", target = "username", qualifiedByName = "getUserName")
    @Mapping(source = "createdAt", target = "joinedAt")
    GetMeetingMemberDto toGetMeetingMemberDto(MeetingMember meetingMember);

    @IterableMapping(qualifiedByName = "memberToDto")
    List<GetMeetingMemberDto> toGetMeetingMembersDto(List<MeetingMember> meetingMember);


    @Mapping(source = "id", target = "inviteUrl", qualifiedByName = "getInviteUrl")
    GetMeetingInviteDto toGetMeetingInviteDto(MeetingInvite meetingInvite);

    @IterableMapping(qualifiedByName = "meetingToDto")
    List<GetMeetingDto> toGetDtos(List<Meeting> meetings);

    @Named("getInviteUrl")
    static String getInviteUrl(UUID value) {
        return "localhost:8080/dp/m/%s".formatted(value.toString());
    }

    @Named("getUserId")
    static Long getUserId(User user) {
        return user.getId();
    }

    @Named("getUserName")
    static String getUserName(User user) {
        return user.getNickname();
    }

}
