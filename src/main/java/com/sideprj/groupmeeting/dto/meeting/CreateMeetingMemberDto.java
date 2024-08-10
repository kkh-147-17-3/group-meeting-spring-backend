package com.sideprj.groupmeeting.dto.meeting;


import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateMeetingMemberDto {
    @NotNull
    private Long meetingId;
    @NotEmpty
    private List<Long> memberIds;
}
