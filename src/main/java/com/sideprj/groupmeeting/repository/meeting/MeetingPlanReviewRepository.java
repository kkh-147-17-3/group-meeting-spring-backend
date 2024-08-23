package com.sideprj.groupmeeting.repository.meeting;

import com.sideprj.groupmeeting.entity.meeting.MeetingPlanReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingPlanReviewRepository extends JpaRepository<MeetingPlanReview, Long> {
    MeetingPlanReview findByMeetingPlanIdAndParticipantId(Long meetingPlanId, Long participantId);
}
