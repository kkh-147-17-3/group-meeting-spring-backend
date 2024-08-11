package com.sideprj.groupmeeting.controller;


import com.sideprj.groupmeeting.dto.user.GetUserDto;
import com.sideprj.groupmeeting.dto.user.UpdateUserDeviceDto;
import com.sideprj.groupmeeting.dto.user.UpdateUserDto;
import com.sideprj.groupmeeting.exceptions.ResourceNotFoundException;
import com.sideprj.groupmeeting.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<GetUserDto> getMyInfo(@AuthenticationPrincipal GetUserDto userInfo) {
        return ResponseEntity.ok(userInfo);
    }

    @PatchMapping("/me")
    public ResponseEntity<GetUserDto> updateMyInfo(
            @AuthenticationPrincipal GetUserDto userInfo,
            @RequestParam String nickname,
            @RequestParam MultipartFile profile
    ) throws IOException {
        var dto = new UpdateUserDto(profile, nickname);
        var updatedUserInfo = userService.update(userInfo.id(), dto);
        return ResponseEntity.ok(updatedUserInfo);
    }

    @GetMapping("/nickname/{nickname}/duplicated")
    public ResponseEntity<Boolean> checkUserNicknameDuplicated(
            @AuthenticationPrincipal GetUserDto userInfo,
            @PathVariable String nickname
    ) {
        var result = userService.checkNicknameDuplicated(nickname);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/me/device")
    public ResponseEntity<Object> updateMyDeviceInfo(
            @AuthenticationPrincipal GetUserDto userInfo,
            @RequestBody UpdateUserDeviceDto dto
    ) throws ResourceNotFoundException {
        userService.updateDeviceInfo(userInfo.id(), dto);
        return ResponseEntity.ok().build();
    }
}
