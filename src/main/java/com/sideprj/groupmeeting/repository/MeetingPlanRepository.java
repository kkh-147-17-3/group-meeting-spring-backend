package com.sideprj.groupmeeting.repository;

import com.sideprj.groupmeeting.entity.meeting.MeetingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingPlanRepository extends JpaRepository<MeetingPlan, Long>{
}
