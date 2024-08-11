package com.sideprj.groupmeeting.controller;

import com.sideprj.groupmeeting.dto.meeting.*;
import com.sideprj.groupmeeting.dto.user.GetUserDto;
import com.sideprj.groupmeeting.exceptions.BadRequestException;
import com.sideprj.groupmeeting.exceptions.ResourceNotFoundException;
import com.sideprj.groupmeeting.exceptions.UnauthorizedException;
import com.sideprj.groupmeeting.service.MeetingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("meeting")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping()
    public ResponseEntity<GetMeetingDto> create(
            @AuthenticationPrincipal GetUserDto userInfo, @RequestParam MultipartFile image, @RequestParam String name
    ) throws BadRequestException, IOException {
        var dto = new CreateMeetingDto(name, image);
        var meetingInfo = meetingService.create(userInfo.id(), dto);
        return ResponseEntity.ok(meetingInfo);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GetMeetingDto> update(
            @PathVariable Long id,
            @AuthenticationPrincipal GetUserDto userInfo,
            @RequestParam MultipartFile image,
            @RequestParam String name
    ) throws UnauthorizedException, IOException, ResourceNotFoundException {
        var dto = new UpdateMeetingDto(id, name, image);
        var meetingInfo = meetingService.update(userInfo.id(), dto);
        return ResponseEntity.ok(meetingInfo);
    }

    @PostMapping("/{id}/plan")
    public ResponseEntity<GetMeetingPlanDto> createPlan(
            @AuthenticationPrincipal GetUserDto userInfo,
            @PathVariable Long id,
            @RequestBody CreateMeetingPlanDto dto
    ) throws UnauthorizedException, BadRequestException, ResourceNotFoundException {
        dto.setMeetingId(id);
        var invite = meetingService.createPlan(userInfo.id(), dto);
        return ResponseEntity.ok(invite);
    }

    @GetMapping("/plan")
    public ResponseEntity<List<GetMeetingPlanDto>> getMyPlan(
            @AuthenticationPrincipal GetUserDto userInfo
    ) {
        var plans = meetingService.getMeetingPlansByParticipantUserId(userInfo.id());
        return ResponseEntity.ok(plans);
    }

    @PostMapping("/{id}/member")
    public ResponseEntity<List<GetMeetingMemberDto>> createMember(
            @AuthenticationPrincipal GetUserDto userInfo,
            @PathVariable Long id,
            @RequestParam CreateMeetingMemberDto dto
    ) throws BadRequestException, ResourceNotFoundException, UnauthorizedException {
        dto.setMeetingId(id);
        var invite = meetingService.createMember(userInfo.id(), dto);
        return ResponseEntity.ok(invite);
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<GetMeetingInviteDto> createInvite(
            @AuthenticationPrincipal GetUserDto userInfo,
            @PathVariable Long id,
            @Valid @RequestBody CreateMeetingInviteDto dto
    ) throws ResourceNotFoundException {

        var invite = meetingService.createInvite(userInfo.id(), id, dto.expiresIn());
        return ResponseEntity.ok(invite);
    }

    @GetMapping("active")
    public ResponseEntity<List<GetMeetingDto>> getMyMeeting(@AuthenticationPrincipal GetUserDto userInfo) {
        var meetings = meetingService.getActiveMeetingsByUserId(userInfo.id());
        return ResponseEntity.ok(meetings);
    }

    @RequestMapping(value = {"/{id}", "/"}, method = RequestMethod.GET)
    public ResponseEntity<GetMeetingDto> getMeetingInfo(
            @PathVariable(value = "id", required = false) Long id,
            @RequestParam UUID inviteId
    ) throws ResourceNotFoundException {
        var meeting = meetingService.findByIdAndInviteId(id, inviteId);
        return ResponseEntity.ok(meeting);
    }

    @PatchMapping("/plan/{id}")
    public ResponseEntity<GetMeetingPlanDto> updatePlan(
            @AuthenticationPrincipal GetUserDto userInfo,
            @PathVariable Long id,
            @RequestBody UpdateMeetingPlanDto dto
    ) throws ResourceNotFoundException, UnauthorizedException {
        dto.setMeetingPlanId(id);
        var result = meetingService.updatePlan(userInfo.id(), dto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/plan/{id}/join")
    public ResponseEntity<GetMeetingPlanDto> joinMeetingPlan(
            @AuthenticationPrincipal GetUserDto userInfo,
            @PathVariable Long id
    ) throws BadRequestException, ResourceNotFoundException {
        var result = meetingService.createPlanParticipant(userInfo.id(), id);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/plan/{id}/join")
    public ResponseEntity<GetMeetingPlanDto> leaveMeetingPlan(
            @AuthenticationPrincipal GetUserDto userInfo,
            @PathVariable Long id
    ) throws BadRequestException, ResourceNotFoundException {
        var result = meetingService.deletePlanParticipant(userInfo.id(), id);
        return ResponseEntity.ok(result);
    }
}
