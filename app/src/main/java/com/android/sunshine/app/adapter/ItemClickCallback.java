package com.android.sunshine.app.adapter;

public interface ItemClickCallback {

    void onItemSelected(long date, final ForecastCursorAdapter.ViewHolder viewHolder);
}
