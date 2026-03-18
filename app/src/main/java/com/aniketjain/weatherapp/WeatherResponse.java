package com.aniketjain.weatherapp;

import java.util.List;

public class WeatherResponse {
    // Đây là các biến mà HomeActivity đang báo thiếu
    public long dt;
    public List<Weather> weather;
    public Main main;
    public Wind wind;
    public Sys sys;
    public String name;

    public class Main {
        public float temp;
        public float temp_min;
        public float temp_max;
        public int humidity;
        public int pressure;
    }

    public class Weather {
        public int id;
        public String main;
        public String description;
        public String icon;
    }

    public class Wind {
        public float speed;
    }

    public class Sys {
        public long sunrise;
        public long sunset;
    }
}