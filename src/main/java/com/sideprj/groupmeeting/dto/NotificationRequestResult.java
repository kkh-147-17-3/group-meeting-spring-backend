package com.sideprj.groupmeeting.dto;

import com.sideprj.groupmeeting.entity.User;

public record NotificationRequestResult(
        Long id,

        boolean isSuccessful,

        String title,
        String deviceToken,
        User.DeviceType deviceType,
        String message,
        String requestData,
        String responseBody
) {}
