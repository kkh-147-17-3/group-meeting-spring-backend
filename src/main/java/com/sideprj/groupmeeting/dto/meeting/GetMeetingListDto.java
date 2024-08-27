package com.sideprj.groupmeeting.dto.meeting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetMeetingListDto{
    private Long id;
    private String name;
    private Long creatorId;
    private String creatorNickname;
    private String imageUrl;
    private List<GetMeetingMemberDto> members;
    private LocalDateTime createdAt;
    private LocalDateTime lastMeetingPlan;
}
