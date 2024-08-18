package com.sideprj.groupmeeting.entity.meeting;

import com.sideprj.groupmeeting.entity.BaseTimeEntity;
import com.sideprj.groupmeeting.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetingPlanComment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User creator;

    @ManyToOne
    private MeetingPlan meetingPlan;

    @Column
    private String content;

    @Column
    private LocalDateTime deletedAt;
}
