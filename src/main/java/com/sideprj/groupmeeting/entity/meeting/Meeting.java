package com.sideprj.groupmeeting.entity.meeting;

import com.sideprj.groupmeeting.entity.BaseTimeEntity;
import com.sideprj.groupmeeting.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;
import java.util.ArrayList;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Meeting extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String mainImageName;

    @Column
    private String name;

    @ManyToOne
    private User creator;

    @OneToMany(mappedBy = "joinedMeeting")
    private List<MeetingMember> members;

    @OneToMany(mappedBy = "meeting")
    private List<MeetingInvite> invites;

    @OneToMany(mappedBy = "meeting")
    private List<MeetingPlan> plans;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean deleted = false;

    public String getImageUrl(){
        return String.format(
                "https://%s.s3.%s.amazonaws.com/%s",
                "meeting-sideproject",
                "ap-northeast-2",
                this.getMainImageName()
        );
    }
}
