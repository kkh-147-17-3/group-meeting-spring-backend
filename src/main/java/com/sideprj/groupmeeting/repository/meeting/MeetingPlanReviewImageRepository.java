package com.sideprj.groupmeeting.repository.meeting;

import com.sideprj.groupmeeting.entity.meeting.MeetingPlanReview;
import com.sideprj.groupmeeting.entity.meeting.MeetingPlanReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingPlanReviewImageRepository extends JpaRepository<MeetingPlanReviewImage, Long> {
    MeetingPlanReviewImage findByReviewId(Long reviewId);
}
