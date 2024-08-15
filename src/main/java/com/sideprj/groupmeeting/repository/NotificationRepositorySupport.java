package com.sideprj.groupmeeting.repository;


import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sideprj.groupmeeting.entity.Notification;
import com.sideprj.groupmeeting.entity.QNotification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class NotificationRepositorySupport {
    private final JPAQueryFactory qf;

    public NotificationRepositorySupport(JPAQueryFactory qf) {this.qf = qf;}

    public List<Notification> findAllNeedToBeSentFromNow(){
        var now = LocalDateTime.now();
        var notification = QNotification.notification;
        return qf.selectFrom(notification)
                .where(notification.scheduledAt.before(now)
                               .and(notification.readAt.isNull())
                               .and(notification.sentAt.isNull())
                )
                .fetch();
    }
}
