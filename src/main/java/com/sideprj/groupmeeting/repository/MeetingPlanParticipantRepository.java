package com.sideprj.groupmeeting.repository;

import com.sideprj.groupmeeting.entity.meeting.MeetingPlanParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingPlanParticipantRepository extends JpaRepository<MeetingPlanParticipant, Long> {
}
