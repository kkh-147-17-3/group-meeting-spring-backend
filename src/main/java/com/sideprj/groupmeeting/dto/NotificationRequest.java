package com.sideprj.groupmeeting.dto;

import com.sideprj.groupmeeting.entity.Notification;
import com.sideprj.groupmeeting.entity.User;

import java.util.HashMap;
import java.util.Map;

public record NotificationRequest(
        Long id,
        String title,
        String message,
        Map<String, Object> data,
        Notification.ActionType actionType,
        String deviceToken,
        User.DeviceType deviceType,
        int badgeCount
){}