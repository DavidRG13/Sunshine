package com.android.sunshine.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.sunshine.app.R;
import com.android.sunshine.app.fragments.ForecastFragment;
import com.android.sunshine.app.utils.Utilities;

public class ForecastCursorAdapter extends CursorAdapter{

    public ForecastCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.forecast_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        final ImageView forecastIcon = (ImageView) view.findViewById(R.id.list_item_icon);
        forecastIcon.setImageResource(R.drawable.ic_launcher);
        final String weatherDate = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
        final TextView dateWeather = (TextView) view.findViewById(R.id.list_item_date);
        dateWeather.setText(Utilities.getFriendlyDay(context, weatherDate));
        final String descriptionWeather = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        final TextView forecastDescription = (TextView) view.findViewById(R.id.list_item_forecast);
        forecastDescription.setText(descriptionWeather);
        final boolean isMetric = Utilities.isMetric(context);
        final float maxTemp = cursor.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP);
        final float minTemp = cursor.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP);
        final TextView max = (TextView) view.findViewById(R.id.list_item_max);
        final TextView min = (TextView) view.findViewById(R.id.list_item_min);
        max.setText(Utilities.formatTemperature(maxTemp, isMetric));
        min.setText(Utilities.formatTemperature(minTemp, isMetric));
    }
}