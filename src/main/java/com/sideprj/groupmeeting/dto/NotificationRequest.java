package com.sideprj.groupmeeting.dto;

import com.sideprj.groupmeeting.entity.Notification;
import com.sideprj.groupmeeting.entity.User;

import java.util.HashMap;

public record NotificationRequest(
        Long id,
        String title,
        String message,
        String data,
        Notification.ActionType actionType,
        String deviceToken,
        User.DeviceType deviceType,
        int badgeCount
){}