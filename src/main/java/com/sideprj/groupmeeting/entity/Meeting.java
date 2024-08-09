package com.sideprj.groupmeeting.entity;

import jakarta.persistence.*;

@Entity
public class Meeting extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column
    String mainImageName;

    @Column
    String name;

    @ManyToOne
    User creator;
}
