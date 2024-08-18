package com.sideprj.groupmeeting.entity.meeting;

import com.sideprj.groupmeeting.entity.BaseTimeEntity;
import com.sideprj.groupmeeting.entity.User;
import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingPlan extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User creator;

    @ManyToOne
    private Meeting meeting;

    @OneToMany(mappedBy = "meetingPlan", cascade = CascadeType.ALL)
    private List<MeetingPlanParticipant> participants;

    @OneToMany(mappedBy = "meetingPlan", cascade = CascadeType.ALL)
    private List<MeetingPlanComment> comments;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String detailAddress;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column
    private Float temperature;

    @Column
    private Integer weatherId;

    @Column
    private String weatherIcon;

    @Column
    private LocalDateTime weatherUpdatedAt;

    public String getWeatherIconUrl(){
        if(weatherIcon == null) return null;
        String ICON_URL = "https://openweathermap.org/img/wn";
        return  "%s/%s@2x.png".formatted(ICON_URL, weatherIcon);
    }

    @Transient
    public List<MeetingPlanComment> getActiveComments() {
        return this.comments.stream()
                            .filter(comment -> comment.getDeletedAt() == null)
                            .toList();
    }
}
