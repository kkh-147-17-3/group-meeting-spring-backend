package com.sideprj.groupmeeting.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sideprj.groupmeeting.entity.meeting.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Repository
public class MeetingRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public MeetingRepositorySupport(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

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

    public Meeting findByIdOrInviteId(Long meetingId, UUID inviteId) {
        var meeting = QMeeting.meeting;
        var meetingInvite = QMeetingInvite.meetingInvite;
        if (meetingId == null && inviteId == null) {
            return null;
        }
        BooleanExpression whereCondition = null;
        if (meetingId != null) {
            whereCondition = (meeting.id.eq(meetingId));
        }
        if (inviteId != null) {
            whereCondition = whereCondition == null ? meetingInvite.id.eq(inviteId)
                                                    : whereCondition.and(meetingInvite.id.eq(inviteId));
        }
        return queryFactory.selectFrom(meeting)
                .join(meeting.members)
                .fetchJoin()
                .innerJoin(meeting.invites, meetingInvite)
                .where(whereCondition)
                .orderBy(
                        meeting.createdAt.desc()
                )
                .fetchOne();
    }

    public List<MeetingPlan> findPlansByParticipantUserId(Long userId, Integer page, YearMonth yearMonth, Boolean closed) {
        var numInPage = 5;
        var offset = 5 * (page - 1);
        var meetingPlan = QMeetingPlan.meetingPlan;
        var meetingPlanParticipant = QMeetingPlanParticipant.meetingPlanParticipant;
        BooleanExpression yearMonthFilter = null;
        BooleanExpression activeFilter = null;

        if(yearMonth != null) {
            LocalDateTime yearMonthStart = yearMonth.atDay(1).atTime(0,0,0);
            LocalDateTime yearMonthEnd = yearMonth.atEndOfMonth().atTime(23,59,59);
            yearMonthFilter = meetingPlan.startAt.between(yearMonthStart, yearMonthEnd);
        }

        if(closed != null){
            var now = LocalDateTime.now();
            activeFilter = closed ? meetingPlan.startAt.before(now) : meetingPlan.startAt.after(now);
        }


        return queryFactory.selectFrom(meetingPlan)
                .innerJoin(meetingPlan.participants, meetingPlanParticipant)
                .fetchJoin()
                .where(meetingPlan.id
                        .in(
                                queryFactory.select(meetingPlan.id)
                                        .from(meetingPlan)
                                        .innerJoin(
                                                meetingPlan.participants,
                                                meetingPlanParticipant
                                        )
                                        .where(meetingPlanParticipant.user.id.eq(userId))
                                        .fetch()
                        )
                        .and(activeFilter)
                        .and(yearMonthFilter)
                )
                .orderBy(meetingPlan.startAt.asc())
                .limit(numInPage)
                .offset(offset)
                .fetch();
    }

    public List<Meeting> findWhereNoPlansFound() {
        QMeeting meeting = QMeeting.meeting;
        QMeetingPlan meetingPlan = QMeetingPlan.meetingPlan;

        var startAt = LocalDateTime.now().minusHours(1);
        var endAt = startAt.plusMinutes(10);

        return queryFactory
                .selectFrom(meeting)
                .where(JPAExpressions
                        .selectOne()
                        .from(meetingPlan)
                        .where(meetingPlan.meeting.id.eq(meeting.id))
                        .notExists()
                        .and(meeting.createdAt.between(startAt, endAt)))
                .fetch();
    }
}
