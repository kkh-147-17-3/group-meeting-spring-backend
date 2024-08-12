package com.sideprj.groupmeeting.controller;

import com.sideprj.groupmeeting.annotation.ApiLogging;
import com.sideprj.groupmeeting.dto.DefaultUserDetails;
import com.sideprj.groupmeeting.dto.meeting.*;
import com.sideprj.groupmeeting.dto.user.GetUserDto;
import com.sideprj.groupmeeting.exceptions.BadRequestException;
import com.sideprj.groupmeeting.exceptions.ResourceNotFoundException;
import com.sideprj.groupmeeting.exceptions.UnauthorizedException;
import com.sideprj.groupmeeting.service.MeetingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
            @AuthenticationPrincipal DefaultUserDetails userDetails, @RequestParam MultipartFile image, @RequestParam String name
    ) throws BadRequestException, IOException {
        var dto = new CreateMeetingDto(name, image);
        var meetingInfo = meetingService.create(userDetails.getId(), dto);
        return ResponseEntity.ok(meetingInfo);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GetMeetingDto> update(
            @PathVariable Long id,
            @AuthenticationPrincipal DefaultUserDetails userDetails,
            @RequestParam MultipartFile image,
            @RequestParam String name
    ) throws UnauthorizedException, IOException, ResourceNotFoundException {
        var dto = new UpdateMeetingDto(id, name, image);
        var meetingInfo = meetingService.update(userDetails.getId(), dto);
        return ResponseEntity.ok(meetingInfo);
    }

    @PostMapping("/{id}/plan")
    public ResponseEntity<GetMeetingPlanDto> createPlan(
            @AuthenticationPrincipal DefaultUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody CreateMeetingPlanDto dto
    ) throws UnauthorizedException, BadRequestException, ResourceNotFoundException {
        dto.setMeetingId(id);
        var invite = meetingService.createPlan(userDetails.getId(), dto);
        return ResponseEntity.ok(invite);
    }

    @GetMapping("/plan")
    @ApiLogging
    public ResponseEntity<List<GetMeetingPlanDto>> getMyPlan(
            @AuthenticationPrincipal DefaultUserDetails userDetails
    ) {
        var plans = meetingService.getMeetingPlansByParticipantUserId(userDetails.getId());
        return ResponseEntity.ok(plans);
    }

    @PostMapping("/{id}/member")
    public ResponseEntity<List<GetMeetingMemberDto>> createMember(
            @AuthenticationPrincipal DefaultUserDetails userDetails,
            @PathVariable Long id,
            @RequestParam CreateMeetingMemberDto dto
    ) throws BadRequestException, ResourceNotFoundException, UnauthorizedException {
        dto.setMeetingId(id);
        var invite = meetingService.createMember(userDetails.getId(), dto);
        return ResponseEntity.ok(invite);
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<GetMeetingInviteDto> createInvite(
            @AuthenticationPrincipal DefaultUserDetails userDetails,
            @PathVariable Long id
    ) throws ResourceNotFoundException {

        var invite = meetingService.createInvite(userDetails.getId(), id);
        return ResponseEntity.ok(invite);
    }

    @GetMapping("active")
    public ResponseEntity<List<GetMeetingDto>> getMyMeeting(@AuthenticationPrincipal DefaultUserDetails userDetails) {
        var meetings = meetingService.getActiveMeetingsByUserId(userDetails.getId());
        return ResponseEntity.ok(meetings);
    }

    @ApiLogging
    @RequestMapping(value = {"/{id}", "/"}, method = RequestMethod.GET)
    public ResponseEntity<GetMeetingDto> getMeetingInfo(
            @PathVariable(value = "id", required = false) Long id,
            @RequestParam(required = false) UUID inviteId
    ) throws ResourceNotFoundException {
        var meeting = meetingService.findByIdAndInviteId(id, inviteId);
        return ResponseEntity.ok(meeting);
    }

    @ApiLogging
    @PatchMapping("/plan/{id}")
    public ResponseEntity<GetMeetingPlanDto> updatePlan(
            @AuthenticationPrincipal DefaultUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody UpdateMeetingPlanDto dto
    ) throws ResourceNotFoundException, UnauthorizedException {
        dto.setMeetingPlanId(id);
        var result = meetingService.updatePlan(userDetails.getId(), dto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/plan/{id}/join")
    public ResponseEntity<GetMeetingPlanDto> joinMeetingPlan(
            @AuthenticationPrincipal DefaultUserDetails userDetails,
            @PathVariable Long id
    ) throws BadRequestException, ResourceNotFoundException {
        var result = meetingService.createPlanParticipant(userDetails.getId(), id);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/plan/{id}/join")
    public ResponseEntity<GetMeetingPlanDto> leaveMeetingPlan(
            @AuthenticationPrincipal DefaultUserDetails userDetails,
            @PathVariable Long id
    ) throws BadRequestException, ResourceNotFoundException {
        var result = meetingService.deletePlanParticipant(userDetails.getId(), id);
        return ResponseEntity.ok(result);
    }
}
