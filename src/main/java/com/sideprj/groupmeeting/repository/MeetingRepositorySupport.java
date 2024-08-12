package com.sideprj.groupmeeting.repository;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sideprj.groupmeeting.entity.meeting.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    public Meeting findByIdOrInviteId(Long meetingId, UUID inviteId) {
        var meeting = QMeeting.meeting;
        var meetingInvite = QMeetingInvite.meetingInvite;
        return queryFactory.selectFrom(meeting)
                           .join(meeting.members)
                           .fetchJoin()
                           .innerJoin(meeting.invites, meetingInvite)
                           .where(
                                   meeting.id.eq(meetingId)
                                             .or(meetingInvite.id.eq(inviteId))

                           )
                           .orderBy(
                                   meeting.createdAt.desc()
                           )
                           .fetchOne();
    }

    public List<MeetingPlan> findPlansByParticipantUserId(Long userId) {
        var meetingPlan = QMeetingPlan.meetingPlan;
        var meetingPlanParticipant = QMeetingPlanParticipant.meetingPlanParticipant;
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
                                          .and(meetingPlan.startAt.after(LocalDateTime.now()))
                           )
                           .orderBy(meetingPlan.startAt.asc())
                           .limit(5)
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
