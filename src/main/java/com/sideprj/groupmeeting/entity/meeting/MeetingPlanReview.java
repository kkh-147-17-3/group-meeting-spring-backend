package com.sideprj.groupmeeting.entity.meeting;


import com.sideprj.groupmeeting.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingPlanReview extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private MeetingPlanParticipant participant;

    @ManyToOne
    private MeetingPlan meetingPlan;

    @Column
    private String contents;

    @Column
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "review", fetch = FetchType.EAGER)
    private List<MeetingPlanReviewImage> images;
}
