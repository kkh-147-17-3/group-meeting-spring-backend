package com.sideprj.groupmeeting.repository.meeting;

import com.sideprj.groupmeeting.entity.meeting.MeetingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingPlanRepository extends JpaRepository<MeetingPlan, Long>{
    public List<MeetingPlan> findByStartAtBetween(LocalDateTime min, LocalDateTime max);
}
