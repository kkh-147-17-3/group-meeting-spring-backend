package com.sideprj.groupmeeting.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class SimpleLoggingAspect {
    @Around("@annotation(com.sideprj.groupmeeting.annotation.ApiLogging)")
    public Object processCustomAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // Log request
        log.info("Request - Method: {} URI: {} Payload: {}",
                 request.getMethod(),
                 request.getRequestURI(),
                 Arrays.toString(joinPoint.getArgs()));

        Object result = joinPoint.proceed();

        // Log response
        log.info("Response - Method: {} URI: {} Payload: {} Duration: {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    result,
                    System.currentTimeMillis() - start);

        return result;
    }
}
