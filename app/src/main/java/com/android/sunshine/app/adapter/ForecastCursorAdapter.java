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
import com.android.sunshine.app.utils.Utilities;

import static com.android.sunshine.app.model.WeatherContract.WeatherEntry;

public class ForecastCursorAdapter extends CursorAdapter{

    private static final int TODAY_VIEW_TYPE = 0;
    private static final int FUTURE_DAY_VIEW_TYPE = 1;

    public ForecastCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final int itemViewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (itemViewType){
            case TODAY_VIEW_TYPE:
                layoutId = R.layout.today_list_item;
                break;
            case FUTURE_DAY_VIEW_TYPE:
                layoutId = R.layout.forecast_list_item;
                break;
        }
        final View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final boolean isMetric = Utilities.isMetric(context);
        final int weatherId = cursor.getInt(cursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID));
        final String weatherDate = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_DATETEXT));
        final String descriptionWeather = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
        final float maxTemp = cursor.getFloat(cursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP));
        final float minTemp = cursor.getFloat(cursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP));

        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.dateWeather.setText(Utilities.getFriendlyDay(context, weatherDate));
        viewHolder.forecastDescription.setText(descriptionWeather);
        viewHolder.max.setText(Utilities.formatTemperature(context, maxTemp, isMetric));
        viewHolder.min.setText(Utilities.formatTemperature(context, minTemp, isMetric));

        System.out.println("weatherId = " + weatherId);
        if(getItemViewType(cursor.getPosition()) == TODAY_VIEW_TYPE){
            viewHolder.forecastIcon.setImageResource(Utilities.getArtResourceForWeatherCondition(weatherId));
        }else{
            viewHolder.forecastIcon.setImageResource(Utilities.getIconResourceForWeatherCondition(weatherId));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? TODAY_VIEW_TYPE : FUTURE_DAY_VIEW_TYPE;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private static class ViewHolder{
        public final ImageView forecastIcon;
        public final TextView dateWeather;
        public final TextView forecastDescription;
        public final TextView max;
        public final TextView min;

        private ViewHolder(View view) {
            forecastIcon = (ImageView) view.findViewById(R.id.list_item_icon);
            dateWeather = (TextView) view.findViewById(R.id.list_item_date);
            forecastDescription = (TextView) view.findViewById(R.id.list_item_forecast);
            max = (TextView) view.findViewById(R.id.list_item_max);
            min = (TextView) view.findViewById(R.id.list_item_min);
        }
    }
}