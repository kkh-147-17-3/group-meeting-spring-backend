package com.sideprj.groupmeeting.controller;

import com.sideprj.groupmeeting.dto.DefaultUserDetails;
import com.sideprj.groupmeeting.dto.meeting.GetMeetingPlanCommentReport;
import com.sideprj.groupmeeting.service.MeetingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("admin")
public class AdminController {
    private final MeetingService meetingService;

    public AdminController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @GetMapping("/meeting/plan/comment/report")
    public ResponseEntity<List<GetMeetingPlanCommentReport>> getAllReports(
            @AuthenticationPrincipal DefaultUserDetails userDetails,
            @RequestParam (required = false, defaultValue = "10") Integer numInPage,
            @RequestParam (required = false, defaultValue = "0") Integer page
    ){
        var results = meetingService.getAllReports(page, numInPage);
        return ResponseEntity.ok(results);
    }
}
