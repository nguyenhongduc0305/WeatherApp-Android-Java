package com.aniketjain.weatherapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout; // Đã đổi thành LinearLayout
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aniketjain.weatherapp.ForecastResponse;
import com.aniketjain.weatherapp.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DaysAdapter extends RecyclerView.Adapter<DaysAdapter.DayViewHolder> {
    private final Context context;
    private final List<ForecastResponse.ForecastItem> forecastList;

    public DaysAdapter(Context context, List<ForecastResponse.ForecastItem> forecastList) {
        this.context = context;
        this.forecastList = forecastList;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.day_item_layout, parent, false);
        return new DayViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        // Kiểm tra an toàn để tránh crash khi list trống
        if (forecastList == null || forecastList.isEmpty()) return;

        // API trả về 40 mốc (3h/mốc), lấy cách 8 item để có dữ liệu các ngày kế tiếp
        int index = position * 8;
        if (index >= forecastList.size()) index = forecastList.size() - 1;

        ForecastResponse.ForecastItem item = forecastList.get(index);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        holder.dTime.setText(sdf.format(new Date(item.dt * 1000)));

        holder.temp_max.setText(Math.round(item.main.temp_max) + "°C");
        holder.temp_min.setText(Math.round(item.main.temp_min) + "°C");

        holder.wind.setText(item.wind.speed + " km/h");
        holder.humidity.setText(item.main.humidity + "%");

        String iconCode = item.weather.get(0).icon;
        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        Picasso.get().load(iconUrl).into(holder.icon);

        holder.progress.setVisibility(View.GONE);
        holder.layout.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return forecastList != null ? 5 : 0;
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        View progress;
        LinearLayout layout; // FIX: Đã sửa kiểu dữ liệu từ RelativeLayout sang LinearLayout
        TextView dTime, temp_min, temp_max, wind, humidity;
        ImageView icon;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            progress = itemView.findViewById(R.id.day_progress_bar);
            layout = (LinearLayout) itemView.findViewById(R.id.day_relative_layout);
            dTime = itemView.findViewById(R.id.day_time);
            temp_min = itemView.findViewById(R.id.day_min_temp);
            temp_max = itemView.findViewById(R.id.day_max_temp);
            wind = itemView.findViewById(R.id.day_wind);
            humidity = itemView.findViewById(R.id.day_humidity);
            icon = itemView.findViewById(R.id.day_icon);
        }
    }
}