package com.sideprj.groupmeeting.controller;

import com.sideprj.groupmeeting.dto.DefaultUserDetails;
import com.sideprj.groupmeeting.dto.user.GetUserDto;
import com.sideprj.groupmeeting.exceptions.ResourceNotFoundException;
import com.sideprj.groupmeeting.exceptions.UnauthorizedException;
import com.sideprj.groupmeeting.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService){
        this.notificationService = notificationService;
    }

    @PostMapping("{id}")
    public ResponseEntity<Object> updateAsRead(@AuthenticationPrincipal DefaultUserDetails userDetails, @PathVariable Long id) throws UnauthorizedException, ResourceNotFoundException {
        notificationService.updateAsRead(userDetails.getId(), id);
        return ResponseEntity.ok().build();
    }
}
