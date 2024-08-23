package com.sideprj.groupmeeting.dto.meeting;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMeetingPlanCommentReport {
    private Long meetingPlanCommentId;
    private Long reporterId;
    private String reasons;
}
