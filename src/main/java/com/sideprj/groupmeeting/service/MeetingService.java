package com.sideprj.groupmeeting.service;

import com.sideprj.groupmeeting.dto.meeting.*;
import com.sideprj.groupmeeting.entity.Notification;
import com.sideprj.groupmeeting.entity.meeting.*;
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

    private final MeetingPlanParticipantRepository meetingPlanParticipantRepository;
    private final MeetingRepositorySupport meetingRepositorySupport;

    private final NotificationRepository notificationRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public MeetingService(
            MeetingRepository meetingRepository,
            UserRepository userRepository,
            AwsS3Service s3Service,
            MeetingPlanRepository meetingPlanRepository,
            MeetingMemberRepository meetingMemberRepository,
            MeetingInviteRepository meetingInviteRepository,
            MeetingRepositorySupport meetingRepositorySupport,
            MeetingPlanParticipantRepository meetingPlanParticipantRepository,
            NotificationRepository notificationRepository
    ) {
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
        this.s3Service = s3Service;
        this.meetingPlanRepository = meetingPlanRepository;
        this.meetingMemberRepository = meetingMemberRepository;
        this.meetingInviteRepository = meetingInviteRepository;
        this.meetingRepositorySupport = meetingRepositorySupport;
        this.meetingPlanParticipantRepository = meetingPlanParticipantRepository;
        this.notificationRepository = notificationRepository;
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

        MeetingPlanParticipant participant = MeetingPlanParticipant.builder()
                                                                   .meetingPlan(meetingPlan)
                                                                   .user(user)
                                                                   .build();

        meetingPlanParticipantRepository.save(participant);
        var title = "%s 모임 약속 추가 알림".formatted(meeting.getName());
        var message = "%s 님이 새로운 약속을 추가했어요. 약속 내용을 확인해주세요!".formatted(user.getNickname());

        var notifications = meeting.getMembers().stream()
                                   .filter(member -> !member.getUser().getId().equals(creatorId))
                                   .map(member -> Notification.builder()
                                                              .user(member.getUser())
                                                              .deviceType(member.getUser().getDeviceType())
                                                              .deviceToken(member.getUser().getDeviceToken())
                                                              .message(message)
                                                              .title(title)
                                                              .actionData(null)
                                                              .scheduledAt(LocalDateTime.now())
                                                              .build())
                                   .toList();

        notificationRepository.saveAll(notifications);

        return MeetingMapper.INSTANCE.toGetPlanDto(meetingPlan);
    }

    @Transactional
    public GetMeetingPlanDto updatePlan(
            Long userId,
            UpdateMeetingPlanDto dto
    ) throws ResourceNotFoundException, UnauthorizedException {
        var meetingPlan = meetingPlanRepository.findById(dto.getMeetingPlanId()).orElseThrow(ResourceNotFoundException::new);

        if(!meetingPlan.getCreator().getId().equals(userId)){
            throw new UnauthorizedException();
        }

        if(dto.getLatitude() != null){
            meetingPlan.setLatitude(dto.getLatitude());
        }
        if(dto.getLongitude() != null){
            meetingPlan.setLongitude(dto.getLongitude());
        }
        if(dto.getStartAt() != null) {
            meetingPlan.setStartAt(dto.getStartAt());
            if(dto.getEndAt() != null) {
                meetingPlan.setEndAt(dto.getEndAt());
            } else {
                LocalDateTime endAt = dto.getStartAt()
                                                         .withHour(11)
                                                         .withMinute(59)
                                                         .withSecond(59)
                                                         .withNano(0);
                meetingPlan.setEndAt(endAt);
            }
        }
        if(dto.getAddress() != null) {
            meetingPlan.setAddress(dto.getAddress());
        }
        if(dto.getDetailAddress() != null) {
            meetingPlan.setDetailAddress(dto.getDetailAddress());
        }
        if(!dto.getName().isBlank()) {
            meetingPlan.setName(dto.getName());
        }

        meetingPlanRepository.save(meetingPlan);

        var title = "%s 모임 약속 수정 알림".formatted(meetingPlan.getMeeting().getName());
        var message = "%s 님이 약속 내용을 수정했어요. 약속 내용을 확인해주세요!".formatted(meetingPlan.getCreator().getNickname());

        var notifications = meetingPlan.getParticipants().stream()
                                   .filter(member -> !member.getUser().getId().equals(userId))
                                   .map(member -> Notification.builder()
                                                              .user(member.getUser())
                                                              .deviceType(member.getUser().getDeviceType())
                                                              .deviceToken(member.getUser().getDeviceToken())
                                                              .message(message)
                                                              .title(title)
                                                              .actionData(null)
                                                              .scheduledAt(LocalDateTime.now())
                                                              .build())
                                   .toList();

        notificationRepository.saveAll(notifications);

        return MeetingMapper.INSTANCE.toGetPlanDto(meetingPlan);
    }

    public GetMeetingInviteDto createInvite(
            Long userId,
            Long meetingId
    ) throws ResourceNotFoundException {
        var expiresIn = 1000 * 60 * 24;
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

        return MeetingMapper.INSTANCE.toGetInviteDto(meetingInvite);
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

        return MeetingMapper.INSTANCE.toGetMembersDto(meetingMembers);
    }

    @Transactional
    public List<GetMeetingDto> getActiveMeetingsByUserId(Long userId) {
        var meetings = meetingRepositorySupport.findByDeletedFalseAndCreatorId(userId);
        return MeetingMapper.INSTANCE.toGetDtos(meetings);
    }

    @Transactional
    public GetMeetingDto findByIdAndInviteId(Long meetingId, UUID id) throws ResourceNotFoundException {
        var meeting = meetingRepositorySupport.findByIdOrInviteId(meetingId, id);
        if (meeting == null) {
            throw new ResourceNotFoundException();
        }
        return MeetingMapper.INSTANCE.toGetDto(meeting);
    }

    @Transactional
    public GetMeetingDto findByInviteId(UUID id) throws ResourceNotFoundException {
        var current = LocalDateTime.now();

        var invite = meetingInviteRepository.findByIdAndExpiredAtAfter(id, current)
                                            .orElseThrow(ResourceNotFoundException::new);
        return MeetingMapper.INSTANCE.toGetDto(invite.getMeeting());
    }

    @Transactional
    public List<GetMeetingPlanDto> getMeetingPlansByParticipantUserId(Long userId) {
        var meetingPlans = meetingRepositorySupport.findPlansByParticipantUserId(userId);
        return MeetingMapper.INSTANCE.toGetPlanDtos(meetingPlans);
    }


    @Transactional
    public GetMeetingPlanDto createPlanParticipant(Long userId, Long planId) throws ResourceNotFoundException, BadRequestException {
        var meetingPlan = meetingPlanRepository.findById(planId).orElseThrow(ResourceNotFoundException::new);

        var isNotJoinedYet = meetingPlan.getParticipants()
                                        .stream()
                                        .filter(participant -> participant.getUser().getId().equals(userId))
                                        .findAny().isEmpty();

        if (!isNotJoinedYet) {
            throw new BadRequestException("이미 참여한 사용자입니다.");
        }
        var user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("존재하지 않는 사용자입니다."));

        var meetingPlanParticipant = MeetingPlanParticipant.builder()
                                                           .meetingPlan(meetingPlan)
                                                           .user(user)
                                                           .build();


        meetingPlanParticipantRepository.save(meetingPlanParticipant);
        var title = "%s 모임 약속 참여 알림".formatted(meetingPlan.getMeeting().getName());
        var message = "%s 님이 약속에 참여하기로 했어요. 변경된 참여 인원을 확인해주세요!".formatted(user.getNickname());

        var notifications = meetingPlan.getParticipants().stream()
                                       .filter(participant -> !participant.getUser().getId().equals(userId))
                                       .map(member -> Notification.builder()
                                                                  .user(member.getUser())
                                                                  .deviceType(member.getUser().getDeviceType())
                                                                  .deviceToken(member.getUser().getDeviceToken())
                                                                  .message(message)
                                                                  .title(title)
                                                                  .actionData(null)
                                                                  .scheduledAt(LocalDateTime.now())
                                                                  .build())
                                       .toList();

        notificationRepository.saveAll(notifications);
        entityManager.refresh(meetingPlan);
        return MeetingMapper.INSTANCE.toGetPlanDto(meetingPlan);
    }

    @Transactional
    public GetMeetingPlanDto deletePlanParticipant(Long userId, Long planId) throws ResourceNotFoundException, BadRequestException {
        var meetingPlan = meetingPlanRepository.findById(planId).orElseThrow(ResourceNotFoundException::new);

        var joinedParticipant = meetingPlan.getParticipants()
                                           .stream()
                                           .filter(participant -> participant.getUser().getId().equals(userId))
                                           .findAny()
                                           .orElseThrow(() -> new BadRequestException("아직 참여하지 않은 사용자입니다."));

        var user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("존재하지 않는 사용자입니다."));

        meetingPlan.getParticipants().remove(joinedParticipant);
        meetingPlanRepository.save(meetingPlan);
        meetingPlanParticipantRepository.delete(joinedParticipant);

        var title = "%s 모임 약속 참여 취소 알림".formatted(meetingPlan.getMeeting().getName());
        var message = "%s 님이 참여하기로 한 약속을 취소했어요. 변경된 참여 인원을 확인해주세요".formatted(user.getNickname());

        var notifications = meetingPlan.getParticipants().stream()
                                       .filter(participant -> !participant.getUser().getId().equals(userId))
                                       .map(member -> Notification.builder()
                                                                  .user(member.getUser())
                                                                  .deviceType(member.getUser().getDeviceType())
                                                                  .deviceToken(member.getUser().getDeviceToken())
                                                                  .message(message)
                                                                  .title(title)
                                                                  .actionData(null)
                                                                  .scheduledAt(LocalDateTime.now())
                                                                  .build())
                                       .toList();

        notificationRepository.saveAll(notifications);
        return MeetingMapper.INSTANCE.toGetPlanDto(meetingPlan);
    }
}
