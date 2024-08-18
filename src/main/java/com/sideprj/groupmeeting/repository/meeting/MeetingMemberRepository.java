package com.sideprj.groupmeeting.repository.meeting;

import com.sideprj.groupmeeting.entity.meeting.MeetingMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingMemberRepository extends JpaRepository<MeetingMember, Long> {
}
