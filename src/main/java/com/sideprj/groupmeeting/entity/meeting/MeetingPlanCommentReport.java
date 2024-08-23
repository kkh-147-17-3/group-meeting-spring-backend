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
public class MeetingPlanCommentReport extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User reporter;

    @ManyToOne
    private MeetingPlanComment comment;

    @Column
    private String originalContents;

    @ManyToOne
    private User subject ;

    @Column
    private String reasons;
}
