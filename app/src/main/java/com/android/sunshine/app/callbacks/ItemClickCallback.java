package com.android.sunshine.app.callbacks;

import com.android.sunshine.app.adapter.ForecastCursorAdapter;

public interface ItemClickCallback {

    void onItemSelected(long date, final ForecastCursorAdapter.ViewHolder viewHolder);
}
