package com.sideprj.groupmeeting.mapper;

import com.sideprj.groupmeeting.entity.User;
import com.sideprj.groupmeeting.entity.meeting.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MeetingMapperTest {
    private Meeting meeting;
    private MeetingPlan meetingPlan;
    private MeetingMember meetingMember;

    private MeetingPlanParticipant meetingPlanParticipant;

    private MeetingInvite meetingInvite;

    private MeetingPlanComment meetingPlanComment;

    @BeforeEach
    void setup() {
        var user = User.builder().id(1L).nickname("testnickname").build();

        meeting = Meeting.builder()
                .id(1L)
                .name("test meeting name")
                .creator(user)
                .mainImageName("test_meeting_img_name.png")
                .build();
        var now = LocalDateTime.now();
        meeting.setCreatedAt(now);
        meetingPlan = MeetingPlan.builder()
                .meeting(meeting)
                .creator(user)
                .name("test plan name")
                .startAt(now.plusHours(3))
                .endAt(null)
                .address("test plan address")
                .detailAddress("test plan detail address")
                .longitude(BigDecimal.valueOf(100.2023921))
                .latitude(BigDecimal.valueOf(100.2023921))
                .build();
        meetingMember = MeetingMember.builder()
                .id(1L)
                .joinedMeeting(meeting)
                .user(user)
                .build();
        meetingMember.setCreatedAt(now.plusHours(1));
        meetingPlanParticipant = MeetingPlanParticipant.builder()
                .id(1L)
                .meetingPlan(meetingPlan)
                .user(user)
                .build();
        meetingInvite = MeetingInvite.builder()
                .id(UUID.randomUUID())
                .meeting(meeting)
                .expiredAt(now.plusHours(10))
                .build();
        meetingPlanComment = MeetingPlanComment.builder()
                .content("test213")
                .creator(user)
                .meetingPlan(meetingPlan)
                .build();
        meetingPlan.setComments(List.of(meetingPlanComment));
    }


    @Test
    void test_ToGetPlanDto() {
        var dto = MeetingMapper.INSTANCE.toGetPlanDto(meetingPlan);
        assertEquals(dto.id(), meetingPlan.getId());
        assertEquals(dto.createdAt(), meetingPlan.getCreatedAt());
        assertEquals(dto.name(), meetingPlan.getName());
        assertEquals(dto.address(), meetingPlan.getAddress());
        assertEquals(dto.detailAddress(), meetingPlan.getDetailAddress());
        assertEquals(dto.startAt(), meetingPlan.getStartAt());
        assertEquals(dto.endAt(), meetingPlan.getEndAt());
        assertEquals(dto.latitude(), meetingPlan.getLatitude());
        assertEquals(dto.longitude(), meetingPlan.getLongitude());
    }

    @Test
    void test_ToGetPlanParticipant() {
        var dto = MeetingMapper.INSTANCE.planParticipantToGetUserDto(meetingPlanParticipant);
        assertEquals(dto.id(), meetingPlanParticipant.getId());
        assertEquals(dto.userProfileImgUrl(), meetingPlanParticipant.getUser().getProfileImgUrl());
        assertEquals(dto.userId(), meetingPlanParticipant.getUser().getId());
        assertEquals(dto.userNickname(), meetingPlanParticipant.getUser().getNickname());
    }


    @Test
    void test_ToGetDto() {
        var dto = MeetingMapper.INSTANCE.toGetDto(meeting);
        assertEquals(meeting.getName(), dto.name());
        assertEquals(meeting.getCreator().getId(), dto.creatorId());
        assertEquals(dto.creatorNickname(), meeting.getCreator().getNickname());
        assertEquals(dto.imageUrl(), meeting.getImageUrl());
        assertEquals(dto.createdAt(), meeting.getCreatedAt());
    }

    @Test
    void test_ToGetMemberDto() {
        var dto = MeetingMapper.INSTANCE.toGetMemberDto(meetingMember);
        assertEquals(dto.id(), meetingMember.getId());
        assertEquals(dto.userId(), meetingMember.getUser().getId());
        assertEquals(dto.userNickname(), meetingMember.getUser().getNickname());
        assertEquals(dto.joinedAt(), meetingMember.getCreatedAt());
    }

    @Test
    void test_ToGetInviteDto() {
        var dto = MeetingMapper.INSTANCE.toGetInviteDto(meetingInvite);
        assertEquals(dto.expiredAt(), meetingInvite.getExpiredAt());
        assertEquals(dto.inviteUrl(), meetingInvite.getInviteUrl());
    }
}
