package com.sideprj.groupmeeting.controller;

import com.sideprj.groupmeeting.dto.TokenSet;
import com.sideprj.groupmeeting.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("apple/login")
    public ResponseEntity<TokenSet> handleAppleLogin(@RequestParam String code) {
        var result = authService.handleAppleLogin(code);
        return ResponseEntity.ok(result);
    }
}
