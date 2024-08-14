package com.sideprj.groupmeeting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sideprj.groupmeeting.dto.GeoLocation;
import com.sideprj.groupmeeting.dto.NotificationRequestResult;
import com.sideprj.groupmeeting.dto.OpenWeatherResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class OpenWeatherService {
    private final OkHttpClient httpClient;

    private final ObjectMapper objectMapper;

    @Value("${openweather.app-id}")
    private String APP_ID;

    public OpenWeatherService(OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Async
    public CompletableFuture<OpenWeatherResponse> getWeatherInfoByLocation(GeoLocation geoLocation) {
        CompletableFuture<OpenWeatherResponse> future = new CompletableFuture<>();


        String API_URL = "https://api.openweathermap.org/data/2.5/forecast";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(API_URL).newBuilder();
        urlBuilder.addQueryParameter("lat", geoLocation.latitude().toString());
        urlBuilder.addQueryParameter("lon", geoLocation.longitude().toString());
        urlBuilder.addQueryParameter("appid", APP_ID);
        String UNITS = "metric";
        urlBuilder.addQueryParameter("units", UNITS);
        String CNT = "40";
        urlBuilder.addQueryParameter("cnt", CNT);

        String url = urlBuilder.build().toString();

        Request httpRequest = new Request.Builder()
                .url(url)
                .build();

        httpClient.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                log.error(e.getMessage());
                future.completeExceptionally(e);
                call.cancel();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    future.completeExceptionally(new IOException("Unexpected code " + response));
                }
                var resBody = response.body();
                if (resBody == null) throw new IOException("No response body found");
                future.complete(objectMapper.readValue(resBody.string(), OpenWeatherResponse.class));

                response.close();
            }
        });
        return future;
    }
}
