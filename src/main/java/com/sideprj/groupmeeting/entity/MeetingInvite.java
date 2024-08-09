package com.sideprj.groupmeeting.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

public class MeetingInvite extends BaseTimeEntity{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column
    LocalDateTime expiredAt;

    @ManyToOne
    Meeting meeting;
}
