package com.sideprj.groupmeeting.controller;


import com.sideprj.groupmeeting.dto.GetUserDto;
import com.sideprj.groupmeeting.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<GetUserDto> getMyInfo(@AuthenticationPrincipal GetUserDto userInfo){
        return ResponseEntity.ok(userInfo);
    }

}
