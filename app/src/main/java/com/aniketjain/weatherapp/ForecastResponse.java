package com.aniketjain.weatherapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ForecastResponse {
    @SerializedName("list")
    public List<ForecastItem> list;

    public static class ForecastItem {
        @SerializedName("dt")
        public long dt;

        @SerializedName("main")
        public Main main;

        @SerializedName("weather")
        public List<Weather> weather;

        @SerializedName("wind")
        public Wind wind;

        @SerializedName("dt_txt")
        public String dtTxt;
    }

    public static class Main {
        public double temp;
        public double temp_min;
        public double temp_max;
        public int pressure;
        public int humidity;
    }

    public static class Weather {
        public String main;
        public String description;
        public String icon;
    }

    public static class Wind {
        public double speed;
    }
}