package com.sideprj.groupmeeting.dto;

public record WeatherInfo(
        Float temperature,
        Integer weatherId,
        String weatherIcon
) {
}
