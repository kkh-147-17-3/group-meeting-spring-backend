package com.sideprj.groupmeeting.service;

import com.sideprj.groupmeeting.dto.meeting.*;
import com.sideprj.groupmeeting.entity.meeting.Meeting;
import com.sideprj.groupmeeting.entity.meeting.MeetingInvite;
import com.sideprj.groupmeeting.entity.meeting.MeetingMember;
import com.sideprj.groupmeeting.entity.meeting.MeetingPlan;
import com.sideprj.groupmeeting.exceptions.BadRequestException;
import com.sideprj.groupmeeting.exceptions.ResourceNotFoundException;
import com.sideprj.groupmeeting.exceptions.UnauthorizedException;
import com.sideprj.groupmeeting.mapper.MeetingMapper;
import com.sideprj.groupmeeting.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final MeetingPlanRepository meetingPlanRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final AwsS3Service s3Service;
    private final MeetingInviteRepository meetingInviteRepository;
    @PersistenceContext
    private EntityManager entityManager;
    private final MeetingRepositorySupport meetingRepositorySupport;

    public MeetingService(
            MeetingRepository meetingRepository,
            UserRepository userRepository,
            AwsS3Service s3Service,
            MeetingPlanRepository meetingPlanRepository,
            MeetingMemberRepository meetingMemberRepository,
            MeetingInviteRepository meetingInviteRepository,
            MeetingRepositorySupport meetingRepositorySupport
    ) {
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
        this.s3Service = s3Service;
        this.meetingPlanRepository = meetingPlanRepository;
        this.meetingMemberRepository = meetingMemberRepository;
        this.meetingInviteRepository = meetingInviteRepository;
        this.meetingRepositorySupport = meetingRepositorySupport;
    }

    @Transactional
    public GetMeetingDto create(Long creatorId, CreateMeetingDto dto) throws BadRequestException, IOException {
        var user = userRepository.findById(creatorId).orElseThrow(() -> new BadRequestException("존재하지 않는 사용자"));
        var imageName = s3Service.uploadImage(null, "meeting-sideproject", "meeting", dto.image());


        var meeting = Meeting.builder()
                             .creator(user)
                             .name(dto.name())
                             .mainImageName(imageName)
                             .build();
        meetingRepository.save(meeting);

        var meetingMember = new MeetingMember();
        meetingMember.setUser(user);
        meetingMember.setJoinedMeeting(meeting);
        meetingMemberRepository.save(meetingMember);
        entityManager.refresh(meeting);
        return MeetingMapper.INSTANCE.toGetDto(meeting);
    }

    public GetMeetingDto update(
            Long creatorId,
            @Valid UpdateMeetingDto dto
    ) throws UnauthorizedException, IOException, ResourceNotFoundException {
        var meeting = meetingRepository.findById(dto.meetingId()).orElseThrow(ResourceNotFoundException::new);
        if (!Objects.equals(meeting.getCreator().getId(), creatorId)) {
            throw new UnauthorizedException();
        }

        if (!dto.name().isEmpty()) {
            meeting.setName(dto.name());
        }

        if (dto.image() != null) {
            var filename = s3Service.uploadImage(null, "meeting-sideproject", "meeting", dto.image());
            meeting.setMainImageName(filename);
        }
        meetingRepository.save(meeting);
        return MeetingMapper.INSTANCE.toGetDto(meeting);
    }

    @Transactional
    public GetMeetingPlanDto createPlan(
            Long creatorId,
            CreateMeetingPlanDto dto
    ) throws BadRequestException, ResourceNotFoundException, UnauthorizedException {
        var user = userRepository.findById(creatorId).orElseThrow(() -> new BadRequestException("존재하지 않는 사용자"));
        var meeting = meetingRepository.findById(dto.getMeetingId()).orElseThrow(ResourceNotFoundException::new);
        meeting.getMembers().stream()
               .filter(member -> member.getUser().getId().equals(user.getId()))
               .findAny().orElseThrow(UnauthorizedException::new);

        LocalDateTime endAt = dto.getEndAt() == null ? dto.getStartAt()
                                       .withHour(11)
                                       .withMinute(59)
                                       .withSecond(59)
                                       .withNano(0) : dto.getEndAt();

        var meetingPlan = MeetingPlan.builder()
                                     .meeting(meeting)
                                     .creator(user)
                                     .name(dto.getName())
                                     .startAt(dto.getStartAt())
                                     .endAt(endAt)
                                     .address(dto.getAddress())
                                     .detailAddress(dto.getDetailAddress())
                                     .longitude(dto.getLongitude())
                                     .latitude(dto.getLatitude())
                                     .build();

        meetingPlanRepository.save(meetingPlan);

        return MeetingMapper.INSTANCE.toGetPlanDto(meetingPlan);
    }

    public GetMeetingInviteDto createInvite(
            Long userId,
            Long meetingId,
            int expiresIn
    ) throws ResourceNotFoundException {
        var meeting = meetingRepository.findById(meetingId).orElseThrow(ResourceNotFoundException::new);

        var expiredAt = Instant.ofEpochMilli(
                                       System.currentTimeMillis() + expiresIn
                               ).atZone(ZoneId.systemDefault())
                               .toLocalDateTime();

        var meetingInvite = MeetingInvite.builder()
                                         .meeting(meeting)
                                         .expiredAt(expiredAt)
                                         .build();

        meetingInviteRepository.save(meetingInvite);

        return MeetingMapper.INSTANCE.toGetMeetingInviteDto(meetingInvite);
    }

    public List<GetMeetingMemberDto> createMember(
            Long userId,
            @Valid CreateMeetingMemberDto dto
    ) throws ResourceNotFoundException, BadRequestException, UnauthorizedException {
        var meeting = meetingRepository.findById(dto.getMeetingId()).orElseThrow(ResourceNotFoundException::new);
        var users = userRepository.findAllById(dto.getMemberIds());

        if (!meeting.getCreator().getId().equals(userId)) {
            throw new UnauthorizedException();
        }


        var alreadyJoinedMembers = meeting.getMembers()
                                          .stream()
                                          .filter(member -> dto.getMemberIds().contains(member.getUser().getId()))
                                          .toList();
        if (!alreadyJoinedMembers.isEmpty()) {
            var alreadyJoinedMemberIds = alreadyJoinedMembers.stream()
                                                             .map(MeetingMember::getId)
                                                             .toList();
            throw new BadRequestException("이미 참여한 사람이 있습니다. userId: %s".formatted(alreadyJoinedMemberIds));
        }

        var meetingMembers = users.stream()
                                  .map((user) -> MeetingMember.builder().joinedMeeting(meeting).user(user).build())
                                  .toList();
        meetingMemberRepository.saveAll(meetingMembers);

        return MeetingMapper.INSTANCE.toGetMeetingMembersDto(meetingMembers);
    }

    @Transactional
    public List<GetMeetingDto> getActiveMeetingsByUserId(Long userId) {
        var meetings = meetingRepositorySupport.findByDeletedFalseAndCreatorId(userId);
        return MeetingMapper.INSTANCE.toGetDtos(meetings);
    }

    @Transactional
    public GetMeetingDto getByInviteId(String id) throws ResourceNotFoundException {
        var current = LocalDateTime.now();
        var invite = meetingInviteRepository.findByIdAndExpiredAtAfter(UUID.fromString(id), current).orElseThrow(ResourceNotFoundException::new);
        return MeetingMapper.INSTANCE.toGetDto(invite.getMeeting());
    }
}
