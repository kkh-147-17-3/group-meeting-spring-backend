package com.sideprj.groupmeeting.controller;

import com.sideprj.groupmeeting.dto.CreateAccessTokenDto;
import com.sideprj.groupmeeting.dto.TokenSet;
import com.sideprj.groupmeeting.exceptions.BadRequestException;
import com.sideprj.groupmeeting.exceptions.UnauthorizedException;
import com.sideprj.groupmeeting.jwt.JwtProvider;
import com.sideprj.groupmeeting.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;


    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @GetMapping("/apple/login")
    public ResponseEntity<TokenSet> handleAppleLogin(@RequestParam String code) throws UnauthorizedException {
        var result = authService.handleAppleLogin(code);
        System.out.println(result);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/access-token")
    public ResponseEntity<String> getAccessToken(HttpServletRequest request, @RequestBody CreateAccessTokenDto dto) throws BadRequestException, UnauthorizedException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        String accessToken = null;

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED);
        } else {
            accessToken = authorization.split(" ")[1];
        }

        var reissuedAccessToken = authService.reissueAccessToken(accessToken, dto.refreshToken());

        return ResponseEntity.ok(reissuedAccessToken);
    }
}
