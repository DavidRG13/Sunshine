package com.android.sunshine.app.adapter;

import android.view.View;

public interface OnAdapterItemClickListener {
    void onClick(long date, final ForecastCursorAdapter.ViewHolder viewHolder);
}
