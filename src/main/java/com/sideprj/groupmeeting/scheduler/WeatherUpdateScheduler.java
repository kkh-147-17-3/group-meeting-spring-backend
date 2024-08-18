package com.sideprj.groupmeeting.scheduler;

import com.sideprj.groupmeeting.dto.GeoLocation;
import com.sideprj.groupmeeting.repository.meeting.MeetingPlanRepository;
import com.sideprj.groupmeeting.repository.meeting.MeetingRepositorySupport;
import com.sideprj.groupmeeting.service.OpenWeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class WeatherUpdateScheduler {
    private final MeetingRepositorySupport meetingRepositorySupport;
    private final MeetingPlanRepository meetingPlanRepository;
    private final OpenWeatherService openWeatherService;


    @Scheduled(fixedDelay = 60 * 1000)
    public void updateMeetingPlanWeatherInfo() {
        var meetingPlans = meetingRepositorySupport.findPlansNeedsWeatherToBeUpdated();
        meetingPlans.forEach(meetingPlan -> {
            openWeatherService.getClosestWeatherInfoFromDateTime(
                    new GeoLocation(meetingPlan.getLongitude(), meetingPlan.getLatitude()),
                    meetingPlan.getStartAt()
            ).thenAcceptAsync(weatherInfo -> {
                if (weatherInfo == null) return;

                meetingPlan.setWeatherIcon(weatherInfo.weatherIcon());
                meetingPlan.setWeatherId(weatherInfo.weatherId());
                meetingPlan.setWeatherUpdatedAt(LocalDateTime.now());
                meetingPlanRepository.save(meetingPlan);
            });

        });
    }
}
