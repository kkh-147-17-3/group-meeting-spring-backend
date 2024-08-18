package com.sideprj.groupmeeting.controller;

import com.sideprj.groupmeeting.annotation.ApiLogging;
import com.sideprj.groupmeeting.dto.DefaultUserDetails;
import com.sideprj.groupmeeting.dto.meeting.*;
import com.sideprj.groupmeeting.exceptions.BadRequestException;
import com.sideprj.groupmeeting.exceptions.ResourceNotFoundException;
import com.sideprj.groupmeeting.exceptions.UnauthorizedException;
import com.sideprj.groupmeeting.service.MeetingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.YearMonth;
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
    ) throws UnauthorizedException, IOException, ResourceNotFoundException, BadRequestException {
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
    public ResponseEntity<List<GetMeetingPlanDto>> getMyPlans(
            @AuthenticationPrincipal DefaultUserDetails userDetails,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMM") YearMonth yearMonth,
            @RequestParam(required = false) Boolean closed
            ) {
        var plans = meetingService.getMeetingPlansByParticipantUserId(userDetails.getId(), page, yearMonth, closed);
        return ResponseEntity.ok(plans);
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<GetMeetingMemberDto> joinMeetingAsMember(
            @AuthenticationPrincipal DefaultUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody JoinMeetingAsMemberDto dto
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

    @GetMapping("/{id}")
    public ResponseEntity<GetMeetingDto> getMeetingInfo(
            @AuthenticationPrincipal DefaultUserDetails userDetails,
            @PathVariable(value = "id") Long id
    ) throws ResourceNotFoundException, UnauthorizedException {
        var meeting = meetingService.findByIdAndUserId(id,userDetails.getId());
        return ResponseEntity.ok(meeting);
    }

    @GetMapping("/plan/{id}")
    public ResponseEntity<GetMeetingPlanDto> getMeetingPlanInfo(
            @AuthenticationPrincipal DefaultUserDetails userDetails,
            @PathVariable(value = "id") Long id
    ) throws ResourceNotFoundException, UnauthorizedException {
        var meeting = meetingService.findPlanById(userDetails.getId(), id);
        return ResponseEntity.ok(meeting);
    }

    @GetMapping("/invite-id/{inviteId}")
    public ResponseEntity<GetMeetingDto> getMeetingInfoByInviteId(
            @PathVariable UUID inviteId
    ) throws ResourceNotFoundException {
        var meeting = meetingService.findByInviteId(inviteId);
        return ResponseEntity.ok(meeting);
    }

    @GetMapping("/{id}/plan")
    public ResponseEntity<List<GetMeetingPlanDto>> getPlansOfMeeting(
            @PathVariable Long id,
            @AuthenticationPrincipal DefaultUserDetails userDetails
    ) throws UnauthorizedException, ResourceNotFoundException {
        List<GetMeetingPlanDto> plans = meetingService.findPlanByMeetingId(userDetails.getId(), id);

        return ResponseEntity.ok(plans);
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

    @PostMapping("/plan/{id}/comment")
    public ResponseEntity<GetMeetingPlanCommentDto> createMeetingPlanComment(
            @AuthenticationPrincipal DefaultUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody CreateMeetingPlanCommentDto dto
    ) throws BadRequestException, ResourceNotFoundException, UnauthorizedException {
        var result = meetingService.createPlanComment(userDetails.getId(), id, dto.content());
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/plan/comment/{id}")
    public ResponseEntity<GetMeetingPlanCommentDto> updateMeetingPlanComment(
            @AuthenticationPrincipal DefaultUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody CreateMeetingPlanCommentDto dto
    ) throws BadRequestException, ResourceNotFoundException, UnauthorizedException {
        var result = meetingService.updateMeetingPlanComment(userDetails.getId(), id, dto.content());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/plan/comment/{id}")
    public ResponseEntity<Void> deleteMeetingPlanComment(
            @AuthenticationPrincipal DefaultUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody CreateMeetingPlanCommentDto dto
    ) throws ResourceNotFoundException, UnauthorizedException {
        meetingService.deleteMeetingPlanComment(userDetails.getId(), id);
        return ResponseEntity.ok().build();
    }
}
