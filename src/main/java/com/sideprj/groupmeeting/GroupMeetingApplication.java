package com.sideprj.groupmeeting;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class GroupMeetingApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroupMeetingApplication.class, args);
    }

    @PostConstruct
    public void init(){
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
