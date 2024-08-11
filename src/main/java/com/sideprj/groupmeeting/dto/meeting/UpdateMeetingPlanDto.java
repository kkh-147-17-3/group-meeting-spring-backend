package com.sideprj.groupmeeting.dto.meeting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMeetingPlanDto {
    private Long meetingPlanId;
    private String name;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String address;
    private String detailAddress;
    private BigDecimal longitude;
    private BigDecimal latitude;
}
