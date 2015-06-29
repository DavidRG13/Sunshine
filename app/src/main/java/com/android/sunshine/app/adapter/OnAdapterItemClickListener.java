package com.android.sunshine.app.adapter;

public interface OnAdapterItemClickListener {
    void onClick(long date, final ForecastCursorAdapter.ViewHolder viewHolder);
}
