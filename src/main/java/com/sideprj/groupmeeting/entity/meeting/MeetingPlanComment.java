package com.sideprj.groupmeeting.entity.meeting;


import com.sideprj.groupmeeting.entity.BaseTimeEntity;
import com.sideprj.groupmeeting.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingPlanComment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User creator;

    @ManyToOne
    private MeetingPlanParticipant participant;

    @ManyToOne
    private MeetingPlan meetingPlan;

    @Column
    private String contents;

    @Column
    private LocalDateTime deletedAt;
}
