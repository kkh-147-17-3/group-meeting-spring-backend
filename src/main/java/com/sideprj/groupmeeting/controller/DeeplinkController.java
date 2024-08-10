package com.sideprj.groupmeeting.controller;

import com.sideprj.groupmeeting.exceptions.ResourceNotFoundException;
import com.sideprj.groupmeeting.service.MeetingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("dp")
public class DeeplinkController {
    private final MeetingService meetingService;


    public DeeplinkController(MeetingService meetingService) {this.meetingService = meetingService;}

    @GetMapping("m/{id}")
    public String getMeetingView(@PathVariable String id, Model model) throws ResourceNotFoundException {
        var meeting = meetingService.getByInviteId(id);
        model.addAttribute("meeting", meeting);
        return "meeting";
    }
}
