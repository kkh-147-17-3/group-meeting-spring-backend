package com.sideprj.groupmeeting.aspect;

import com.sideprj.groupmeeting.DiscordMessageService;
import com.sideprj.groupmeeting.dto.DiscordMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Aspect
@Component
public class SimpleLoggingAspect {

    private final DiscordMessageService messageService;

    public SimpleLoggingAspect(DiscordMessageService messageService) {this.messageService = messageService;}

    @Around("@annotation(com.sideprj.groupmeeting.annotation.ApiLogging)")
    public Object processCustomAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // Log request
        log.info(
                "Request - Method: {} URI: {} Payload: {}",
                request.getMethod(),
                request.getRequestURI(),
                Arrays.toString(joinPoint.getArgs())
        );

        Object result = joinPoint.proceed();

        // Log response
        log.info(
                "Response - Method: {} URI: {} Payload: {} Duration: {}ms",
                request.getMethod(),
                request.getRequestURI(),
                result,
                System.currentTimeMillis() - start
        );

        return result;
    }

    @AfterThrowing(pointcut = "execution(* com.sideprj.groupmeeting.controller..*.*(..))", throwing = "ex")
    public void processControllerExceptionThrown(JoinPoint point, Throwable ex) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // Log request
        log.info(
                "Request - Method: {} URI: {} Payload: {}",
                request.getMethod(),
                request.getRequestURI(),
                Arrays.toString(point.getArgs())
        );

        String message = ("""
                Request - Method: %s\s
                URI: %s
                Arguments: %s, exception: %s(message=%s)
                Stacktrace: %s
                """)
                .formatted(
                        request.getMethod(),
                        request.getRequestURI(),
                        Arrays.toString(point.getArgs()),
                        ex.getClass(),
                        ex.getMessage(),
                        Arrays.toString(new String[]{Arrays.toString(ex.getStackTrace()).substring(0, 1000)})
                );

        var discordMessage = DiscordMessage
                .builder()
                .content("#Server exception occurs")
                .embeds(
                        List.of(
                                DiscordMessage.Embed.builder()
                                                    .title("ℹ️Info")
                                                    .description(message)
                                                    .build()
                        )
                )
                .build();

        messageService.sendMessage(discordMessage);
    }
}
