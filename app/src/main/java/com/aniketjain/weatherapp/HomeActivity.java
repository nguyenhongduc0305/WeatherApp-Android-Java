package com.aniketjain.weatherapp;

import static com.aniketjain.weatherapp.location.CityFinder.getCityNameUsingNetwork;
import static com.aniketjain.weatherapp.location.CityFinder.setLongitudeLatitude;
import static com.aniketjain.weatherapp.network.InternetConnectivity.isInternetConnected;

import android.Manifest;
import android.annotation.SuppressLint;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aniketjain.weatherapp.adapter.DaysAdapter;
import com.aniketjain.weatherapp.databinding.ActivityHomeBinding;
import com.aniketjain.weatherapp.toast.Toaster;
import com.aniketjain.weatherapp.update.UpdateUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1;
    private ActivityHomeBinding binding;

    private String name, updated_at, description, temperature,
            min_temperature, max_temperature, pressure, wind_speed, humidity;

    private String city = "";
    private final String API_KEY = "65380fce5ef3d6263263c440176b7925";

    private ValueAnimator backgroundAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setAnimatedBackground("default");

        binding.dayRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.dayRv.setNestedScrollingEnabled(false);

        setNavigationBarColor();
        setRefreshLayoutColor();
        setListeners();

        if (isInternetConnected(this)) {
            getDataUsingNetwork();
        } else {
            Toaster.errorToast(this, "No Internet!");
        }
    }

    private void getWeather(String cityName) {
        showProgressBar();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi weatherApi = retrofit.create(WeatherApi.class);
        weatherApi.getWeather(cityName, API_KEY, "metric").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    parseWeatherData(response.body());
                } else {
                    hideProgressBar();
                    Toaster.errorToast(HomeActivity.this, "City not found!");
                }
            }
            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                hideProgressBar();
                Toaster.errorToast(HomeActivity.this, "Network Error!");
            }
        });

        weatherApi.getForecast(cityName, API_KEY, "metric").enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(@NonNull Call<ForecastResponse> call, @NonNull Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DaysAdapter adapter = new DaysAdapter(HomeActivity.this, response.body().list);
                    binding.dayRv.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ForecastResponse> call, @NonNull Throwable t) {
                Log.e("ForecastError", t.getMessage());
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private void parseWeatherData(WeatherResponse data) {
        this.name = data.name;
        long updateTime = data.dt;
        updated_at = new SimpleDateFormat("EEEE hh:mm a", Locale.ENGLISH).format(new Date(updateTime * 1000));
        description = data.weather.get(0).main;

        setAnimatedBackground(description);

        temperature = String.valueOf(Math.round(data.main.temp));
        min_temperature = String.format("%.0f", data.main.temp_min);
        max_temperature = String.format("%.0f", data.main.temp_max);
        pressure = String.valueOf(data.main.pressure);
        humidity = String.valueOf(data.main.humidity);
        wind_speed = String.valueOf(data.wind.speed);

        String iconCode = data.weather.get(0).icon;
        int resId = getLocalIcon(iconCode);
        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@4x.png";

        Picasso.get()
                .load(iconUrl)
                .fit()
                .centerInside()
                .placeholder(resId)
                .error(resId)
                .into(binding.layout.conditionIv);

        updateUI();
        hideProgressBar();
    }

    private int getLocalIcon(String iconCode) {
        switch (iconCode) {
            case "01d": return R.drawable.clear_day;
            case "01n": return R.drawable.clear_night;
            case "02d": return R.drawable.few_clouds_day;
            case "02n": return R.drawable.few_clouds_night;
            case "03d":
            case "03n": return R.drawable.scattered_clouds;
            case "04d":
            case "04n": return R.drawable.broken_clouds;
            case "09d":
            case "09n": return R.drawable.drizzle;
            case "10d":
            case "10n": return R.drawable.rain;
            case "11d":
            case "11n": return R.drawable.thunderstorm;
            case "13d":
            case "13n": return R.drawable.snow;
            default: return R.drawable.clear_day;
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {

        binding.layout.weatherDataScroll.setVisibility(View.VISIBLE);

        binding.layout.nameTv.setText(name);
        binding.layout.updatedAtTv.setText(translate(updated_at));
        binding.layout.conditionDescTv.setText(description);
        binding.layout.tempTv.setText(temperature + "°C");
        binding.layout.minTempTv.setText("Min: " + min_temperature + "°C");
        binding.layout.maxTempTv.setText("Max: " + max_temperature + "°C");
        binding.layout.pressureTv.setText(pressure + " mb");
        binding.layout.windTv.setText(wind_speed + " km/h");
        binding.layout.humidityTv.setText(humidity + "%");
    }

    private void setAnimatedBackground(String condition) {
        if (backgroundAnimator != null) {
            backgroundAnimator.end();
        }

        int[] colors;
        switch (condition.toLowerCase()) {
            case "clear": colors = new int[]{Color.parseColor("#FFD54F"), Color.parseColor("#FF8F00")}; break;
            case "clouds": colors = new int[]{Color.parseColor("#546E7A"), Color.parseColor("#263238")}; break;
            case "rain":
            case "drizzle": colors = new int[]{Color.parseColor("#4FC3F7"), Color.parseColor("#1565C0")}; break;
            case "snow": colors = new int[]{Color.parseColor("#E0E0E0"), Color.parseColor("#9E9E9E")}; break;
            case "thunderstorm": colors = new int[]{Color.parseColor("#455A64"), Color.parseColor("#000000")}; break;
            case "default":
            default: colors = new int[]{Color.parseColor("#283593"), Color.parseColor("#121212")};
        }

        final GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        binding.mainLayout.setBackground(gradient);

        backgroundAnimator = ValueAnimator.ofFloat(0f, 1f);
        backgroundAnimator.setDuration(4500);
        backgroundAnimator.setRepeatCount(ValueAnimator.INFINITE);
        backgroundAnimator.setRepeatMode(ValueAnimator.REVERSE);

        final int startColor = colors[0];
        final int endColor = colors[1];
        backgroundAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            int blended = (int) new ArgbEvaluator().evaluate(fraction, startColor, endColor);
            gradient.setColors(new int[]{blended, endColor});
        });
        backgroundAnimator.start();
    }

    private void setListeners() {
        binding.layout.searchBarIv.setOnClickListener(v -> {
            String cityName = binding.layout.cityEt.getText().toString().trim();
            if (!cityName.isEmpty()) {
                getWeather(cityName);
                hideKeyboard(v);
                binding.layout.cityEt.setText("");
            } else {
                Toaster.errorToast(this, "Please enter city name!");
            }
        });

        binding.mainRefreshLayout.setOnRefreshListener(() -> {
            getDataUsingNetwork();
            binding.mainRefreshLayout.setRefreshing(false);
        });
    }

    private void getDataUsingNetwork() {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE);
        } else {
            client.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    setLongitudeLatitude(location);
                    city = getCityNameUsingNetwork(this, location);
                    getWeather(city);
                }
            });
        }
    }

    private void showProgressBar() {
        binding.progress.setVisibility(View.VISIBLE);
        binding.mainRefreshLayout.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        binding.progress.setVisibility(View.GONE);
        binding.mainRefreshLayout.setVisibility(View.VISIBLE);
    }

    private String translate(String day) {
        try {
            String[] split = day.split(" ");
            split[0] = UpdateUI.TranslateDay(split[0], getApplicationContext());
            return split[0] + " " + split[1];
        } catch (Exception e) {
            return day;
        }
    }

    private void setRefreshLayoutColor() {
        binding.mainRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.textColor));
        binding.mainRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.navBarColor));
    }

    private void setNavigationBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.navBarColor));
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isInternetConnected(this)) {
            getDataUsingNetwork();
        }
    }
}