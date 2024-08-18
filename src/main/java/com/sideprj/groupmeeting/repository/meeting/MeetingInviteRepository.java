package com.sideprj.groupmeeting.repository.meeting;

import com.sideprj.groupmeeting.entity.meeting.MeetingInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeetingInviteRepository extends JpaRepository<MeetingInvite, UUID> {
    Optional<MeetingInvite> findByIdAndExpiredAtAfter(UUID uuid, LocalDateTime expiredAt);
}
