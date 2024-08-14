package com.sideprj.groupmeeting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OpenWeatherResponse {

    private String cod;
    private int message;
    private int cnt;
    private List<WeatherList> list;
    private City city;

    // Getters and Setters

    @Getter
    @Setter
    public static class WeatherList {
        private long dt;
        private Main main;
        private List<Weather> weather;
        private Clouds clouds;
        private Wind wind;
        private int visibility;
        private float pop;
        private Rain rain;
        private Sys sys;

        @JsonProperty("dt_txt")
        private String dtTxt;

        // Getters and Setters

        @Getter
        @Setter
        public static class Main {
            private float temp;
            private float feelsLike;
            private float tempMin;
            private float tempMax;
            private int pressure;
            private int seaLevel;
            private int grndLevel;
            private int humidity;
            private float tempKf;

            // Getters and Setters
        }

        @Getter
        @Setter
        public static class Weather {
            private int id;
            private String main;
            private String description;
            private String icon;

        }

        @Getter
        @Setter
        public static class Clouds {
            private int all;

            // Getters and Setters
        }

        @Getter
        @Setter
        public static class Wind {
            private float speed;
            private int deg;
            private float gust;

            // Getters and Setters
        }

        @Getter
        @Setter
        public static class Rain {
            @JsonProperty("3h")
            private float threeHour;

            // Getters and Setters
        }

        @Getter
        @Setter
        public static class Sys {
            private String pod;

            // Getters and Setters
        }
    }

    @Getter
    @Setter
    public static class City {
        private int id;
        private String name;
        private Coord coord;
        private String country;
        private int population;
        private int timezone;
        private long sunrise;
        private long sunset;

        // Getters and Setters

        @Getter
        @Setter
        public static class Coord {
            private double lat;
            private double lon;

            // Getters and Setters
        }
    }
}