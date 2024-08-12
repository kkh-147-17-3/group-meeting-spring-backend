package com.sideprj.groupmeeting.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sideprj.groupmeeting.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    private final UserRepository userRepository;

    private final ObjectMapper objectMapper;

    @Autowired
    public JwtFilter(JwtProvider jwtProvider, UserRepository userRepository, ObjectMapper objectMapper) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException, AuthenticationException {
        String path = request.getServletPath();

        // Skip for login paths
        if (path.equals("/") || path.startsWith("/auth") || path.startsWith("/dp")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token;
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            jwtExceptionHandler(response, 40002);
            return;
        }

        token = authorization.split(" ")[1];


        if (path.startsWith("/auth/token")) {
            if (jwtProvider.isExpired(token)) {
                filterChain.doFilter(request, response);
                return;
            } else {
                throw new RuntimeException("Token is not yet expired");
            }
        }

        if (jwtProvider.isExpired(token)) {
            jwtExceptionHandler(response, 40003);
            return;
        }

        if (!jwtProvider.isAccessToken(token)) {
            jwtExceptionHandler(response, 40004);
            return;
        }
//
//        Long userId = jwtProvider.getUserId(token);
//
//        var user = userRepository.findByIdAndActiveTrue(userId);
//        if (user.isEmpty()) {
//            jwtExceptionHandler(response, 40004);
//            return;
//        }
//
//        GetUserDto userInfo = mapper.toGetDto(user.get());

//        List<SimpleGrantedAuthority> authorities;
//        if (PhoneNoUtils.remainNumberOnly(userInfo.getMobile()).equals("01089628547")) {
//            authorities = Collections.singletonList(new SimpleGrantedAuthority("ADMIN"));
//        } else {
//            authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));
//        }
//        List<SimpleGrantedAuthority> authorities;
//        authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));

        Authentication authentication = jwtProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private void jwtExceptionHandler(HttpServletResponse response, int errorCode) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String json = objectMapper.writeValueAsString(Collections.singletonMap("code", errorCode));
        response.getWriter().write(json);
    }
}