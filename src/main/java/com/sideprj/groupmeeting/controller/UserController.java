package com.sideprj.groupmeeting.controller;


import com.sideprj.groupmeeting.dto.DefaultUserDetails;
import com.sideprj.groupmeeting.dto.user.GetUserDto;
import com.sideprj.groupmeeting.dto.user.UpdateUserDeviceDto;
import com.sideprj.groupmeeting.dto.user.UpdateUserDto;
import com.sideprj.groupmeeting.exceptions.BadRequestException;
import com.sideprj.groupmeeting.exceptions.ResourceNotFoundException;
import com.sideprj.groupmeeting.service.AuthService;
import com.sideprj.groupmeeting.service.NicknameService;
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
    private final NicknameService nicknameService;

    private final AuthService authService;

    public UserController(UserService userService, NicknameService nicknameService, AuthService authService) {
        this.userService = userService;
        this.nicknameService = nicknameService;
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<GetUserDto> getMyInfo(@AuthenticationPrincipal DefaultUserDetails userDetails) throws ResourceNotFoundException {
        var userInfo = userService.get(userDetails.getId());
        return ResponseEntity.ok(userInfo);
    }

    @PatchMapping("/me")
    public ResponseEntity<GetUserDto> updateMyInfo(
            @AuthenticationPrincipal DefaultUserDetails userDetails,
            @RequestParam String nickname,
            @RequestParam MultipartFile profileImg
    ) throws IOException, BadRequestException {
        var dto = new UpdateUserDto(profileImg, nickname);
        var updatedUserInfo = userService.update(userDetails.getId(), dto);
        return ResponseEntity.ok(updatedUserInfo);
    }

    @GetMapping("/nickname/{nickname}/duplicated")
    public ResponseEntity<Boolean> checkUserNicknameDuplicated(
            @AuthenticationPrincipal DefaultUserDetails userDetails,
            @PathVariable String nickname
    ) {
        var result = userService.checkNicknameDuplicated(nickname);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/me/device")
    public ResponseEntity<Object> updateMyDeviceInfo(
            @AuthenticationPrincipal DefaultUserDetails userDetails,
            @RequestBody UpdateUserDeviceDto dto
    ) throws ResourceNotFoundException {
        userService.updateDeviceInfo(userDetails.getId(), dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/nickname/random")
    public ResponseEntity<String> getRandomNickname(){
        var nickname = nicknameService.generateRandomNickname();
        return ResponseEntity.ok(nickname);
    }

    @GetMapping("/{id}/token")
    public ResponseEntity<String> getToken(@PathVariable Long id, @RequestParam(required = false) int expiresIn){
        return ResponseEntity.ok(authService.testToken(id, expiresIn));
    }
}
