package com.android.sunshine.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.sunshine.app.R;
import com.android.sunshine.app.utils.Utilities;

import static com.android.sunshine.app.model.WeatherContract.WeatherEntry;

public class ForecastCursorAdapter extends RecyclerView.Adapter<ForecastCursorAdapter.ViewHolder> implements OnItemClickHandler {

    private static final int TODAY_VIEW_TYPE = 0;
    private static final int FUTURE_DAY_VIEW_TYPE = 1;
    private boolean useTodayLayout;
    private Cursor cursor;
    private Context context;
    private View emptyView;
    private OnAdapterItemClickListener onAdapterItemClickListener;

    public ForecastCursorAdapter(final Context context, final View emptyView, final OnAdapterItemClickListener onAdapterItemClickListener) {
        this.context = context;
        this.emptyView = emptyView;
        this.onAdapterItemClickListener = onAdapterItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int viewType) {
        if (viewGroup instanceof RecyclerView) {
            int layoutId = -1;
            switch (viewType){
                case TODAY_VIEW_TYPE:
                    layoutId = R.layout.today_list_item;
                    break;
                case FUTURE_DAY_VIEW_TYPE:
                    layoutId = R.layout.forecast_list_item;
                    break;
            }
            final View view = LayoutInflater.from(context).inflate(layoutId, viewGroup, false);
            return new ViewHolder(view, this);
        } else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        cursor.moveToPosition(position);

        final boolean isMetric = Utilities.isMetric(context);
        final int weatherId = cursor.getInt(cursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID));
        final long weatherDate = cursor.getInt(cursor.getColumnIndex(WeatherEntry.COLUMN_DATE));
        final String descriptionWeather = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
        final float maxTemp = cursor.getFloat(cursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP));
        final float minTemp = cursor.getFloat(cursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP));

        viewHolder.dateWeather.setText(Utilities.getFriendlyDay(context, weatherDate));
        viewHolder.forecastDescription.setText(descriptionWeather);
        viewHolder.max.setText(Utilities.formatTemperature(context, maxTemp, isMetric));
        viewHolder.min.setText(Utilities.formatTemperature(context, minTemp, isMetric));

        if(getItemViewType(cursor.getPosition()) == TODAY_VIEW_TYPE){
            viewHolder.forecastIcon.setImageResource(Utilities.getArtResourceForWeatherCondition(weatherId));
        }else{
            viewHolder.forecastIcon.setImageResource(Utilities.getIconResourceForWeatherCondition(weatherId));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && useTodayLayout) ? TODAY_VIEW_TYPE : FUTURE_DAY_VIEW_TYPE;
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.useTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemCount() {
        if (null == cursor) {
            return 0;
        } else {
            return cursor.getCount();
        }
    }

    public void swapCursor(final Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
        emptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return cursor;
    }

    @Override
    public void itemClick(final int position, final View view) {
        cursor.moveToPosition(position);
        long date = cursor.getLong(cursor.getColumnIndex(WeatherEntry.COLUMN_DATE));
        onAdapterItemClickListener.onClick(date, position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView forecastIcon;
        public final TextView dateWeather;
        public final TextView forecastDescription;
        public final TextView max;
        public final TextView min;
        private final OnItemClickHandler onItemClickHandler;

        private ViewHolder(View view, final OnItemClickHandler onItemClickHandler) {
            super(view);
            this.onItemClickHandler = onItemClickHandler;
            view.setOnClickListener(this);
            forecastIcon = (ImageView) view.findViewById(R.id.list_item_icon);
            dateWeather = (TextView) view.findViewById(R.id.list_item_date);
            forecastDescription = (TextView) view.findViewById(R.id.list_item_forecast);
            max = (TextView) view.findViewById(R.id.list_item_max);
            min = (TextView) view.findViewById(R.id.list_item_min);
        }

        @Override
        public void onClick(final View view) {
            int position = getAdapterPosition();
            onItemClickHandler.itemClick(position, view);
        }
    }
}