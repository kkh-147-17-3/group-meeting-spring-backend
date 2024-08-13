package com.sideprj.groupmeeting.dto.meeting;


import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JoinMeetingAsMemberDto {
    @NotNull
    private Long meetingId;
    @NotEmpty
    private UUID inviteId;
}
