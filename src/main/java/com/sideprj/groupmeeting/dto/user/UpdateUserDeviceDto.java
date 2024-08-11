package com.sideprj.groupmeeting.dto.user;

import com.sideprj.groupmeeting.entity.User;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateUserDeviceDto(@NotEmpty String deviceToken, @NotNull User.DeviceType deviceType) {}
