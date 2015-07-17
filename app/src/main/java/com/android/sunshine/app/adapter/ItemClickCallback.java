package com.android.sunshine.app.adapter;

import com.android.sunshine.app.utils.WeatherDetails;

public interface ItemClickCallback {

    void onItemSelected(WeatherDetails weatherDetails, final ForecastCursorAdapter.ViewHolder viewHolder);
}
