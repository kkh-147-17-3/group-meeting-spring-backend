package com.sideprj.groupmeeting.dto;

import com.sideprj.groupmeeting.entity.Notification;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationBody {

    private Notification.ActionType actionType;
    private Map<String, Object> data;
    private Long notificationId;


    public NotificationBody(Notification.ActionType actionType, Map<String, Object> data){
        this.actionType = actionType;
        this.data = data;
        this.notificationId = null;
    }
}
