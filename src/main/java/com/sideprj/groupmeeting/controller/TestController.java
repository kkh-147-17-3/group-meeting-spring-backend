package com.sideprj.groupmeeting.controller;

import com.sideprj.groupmeeting.dto.GeoLocation;
import com.sideprj.groupmeeting.dto.OpenWeatherResponse;
import com.sideprj.groupmeeting.service.OpenWeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
public class TestController {
    @Autowired
    OpenWeatherService service;

    @PostMapping("/weather")
    public ResponseEntity<OpenWeatherResponse> getWeather(@RequestBody GeoLocation location) {
        var result = service.getWeatherInfoByLocation(location);
        var res = result.join();
        return ResponseEntity.ok(res);
    }
}
