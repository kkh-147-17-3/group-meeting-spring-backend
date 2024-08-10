package com.sideprj.groupmeeting.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sideprj.groupmeeting.entity.meeting.Meeting;
import com.sideprj.groupmeeting.entity.meeting.QMeeting;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MeetingRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public MeetingRepositorySupport(JPAQueryFactory queryFactory) {this.queryFactory = queryFactory;}

    public List<Meeting> findByDeletedFalseAndCreatorId(Long creatorId) {
        var meeting = QMeeting.meeting;
        return queryFactory.selectFrom(meeting)
                           .leftJoin(meeting.members)
                           .fetchJoin()
                           .where(
                                   meeting.creator.id.eq(creatorId)
                                                     .and(meeting.deleted.isFalse())

                           )
                           .orderBy(
                                   meeting.createdAt.desc()
                           )
                           .fetch();
    }
}
