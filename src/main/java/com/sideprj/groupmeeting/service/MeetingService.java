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
import java.time.YearMonth;
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

    private final MeetingMapper mapper;

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
            NotificationRepository notificationRepository, MeetingMapper mapper
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
        this.mapper = mapper;
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
        return mapper.toGetDto(meeting);
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
        return mapper.toGetDto(meeting);
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

        return mapper.toGetPlanDto(meetingPlan);
    }

    @Transactional
    public GetMeetingPlanDto updatePlan(
            Long userId,
            UpdateMeetingPlanDto dto
    ) throws ResourceNotFoundException, UnauthorizedException {
        var meetingPlan = meetingPlanRepository.findById(dto.getMeetingPlanId()).orElseThrow(ResourceNotFoundException::new);

        if (!meetingPlan.getCreator().getId().equals(userId)) {
            throw new UnauthorizedException();
        }

        if (dto.getLatitude() != null) {
            meetingPlan.setLatitude(dto.getLatitude());
        }
        if (dto.getLongitude() != null) {
            meetingPlan.setLongitude(dto.getLongitude());
        }
        if (dto.getStartAt() != null) {
            meetingPlan.setStartAt(dto.getStartAt());
            if (dto.getEndAt() != null) {
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
        if (dto.getAddress() != null) {
            meetingPlan.setAddress(dto.getAddress());
        }
        if (dto.getDetailAddress() != null) {
            meetingPlan.setDetailAddress(dto.getDetailAddress());
        }
        if (!dto.getName().isBlank()) {
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

        return mapper.toGetPlanDto(meetingPlan);
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

        return mapper.toGetInviteDto(meetingInvite);
    }

    @Transactional
    public GetMeetingMemberDto createMember(
            Long userId,
            @Valid JoinMeetingAsMemberDto dto
    ) throws ResourceNotFoundException, BadRequestException, UnauthorizedException {
        var meeting = meetingRepository.findById(dto.getMeetingId()).orElseThrow(ResourceNotFoundException::new);
        var user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("존재하지 않는 사용자입니다."));
        var meetingInvite = meetingInviteRepository.findByIdAndExpiredAtAfter(dto.getInviteId(), LocalDateTime.now())
                .orElseThrow(()->new BadRequestException("존재하지 않는 초대 코드입니다."));

        if(meeting.getMembers().stream().anyMatch(m->m.getUser().getId().equals(userId))){
            throw new BadRequestException("이미 참여한 사용자입니다.");

        }

        if (!meetingInvite.getMeeting().getId().equals(dto.getMeetingId())){
            throw new BadRequestException("존재하지 않는 초대코드 입니다.");
        }

        var message = "모임 참여 알림";
        var title = "%s님이 %s 모임에 참여했어요.".formatted(user.getNickname(), meeting.getName());

        var notifications = meeting.getMembers()
                .stream()
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


        var meetingMember = MeetingMember.builder().joinedMeeting(meeting).user(user).build();

        meetingMemberRepository.save(meetingMember);
        notificationRepository.saveAll(notifications);

        return mapper.toGetMemberDto(meetingMember);
    }

    @Transactional
    public List<GetMeetingDto> getActiveMeetingsByUserId(Long userId) {
        var meetings = meetingRepositorySupport.findByDeletedFalseAndCreatorId(userId);
        return mapper.toGetDtos(meetings);
    }

    @Transactional
    public GetMeetingDto findById(Long meetingId) throws ResourceNotFoundException {
        var meeting = meetingRepository.findById(meetingId).orElseThrow(ResourceNotFoundException::new);
        return mapper.toGetDto(meeting);
    }

    @Transactional
    public GetMeetingDto findByInviteId(UUID id) throws ResourceNotFoundException {
        var current = LocalDateTime.now();

        var invite = meetingInviteRepository.findByIdAndExpiredAtAfter(id, current)
                .orElseThrow(ResourceNotFoundException::new);
        return mapper.toGetDto(invite.getMeeting());
    }

    @Transactional
    public List<GetMeetingPlanDto> getMeetingPlansByParticipantUserId(Long userId, Integer page, YearMonth yearMonth, Boolean closed) {
        page = page != null ? page : 1;
        var meetingPlans = meetingRepositorySupport.findPlansByParticipantUserId(userId, page, yearMonth, closed);
        return mapper.toGetPlanDtos(meetingPlans);
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
        return mapper.toGetPlanDto(meetingPlan);
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
        return mapper.toGetPlanDto(meetingPlan);
    }
}
