package com.sideprj.groupmeeting.entity.meeting;


import com.sideprj.groupmeeting.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingPlanReviewImage extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private MeetingPlanReview review;

    @ManyToOne
    private MeetingPlan meetingPlan;

    @Column
    private String fileName;

    public String getImageUrl(){
        if(getFileName() == null) return null;

        return String.format(
                "https://%s.s3.%s.amazonaws.com/%s",
                "meeting-sideproject",
                "ap-northeast-2",
                this.getFileName()
        );
    }
}
