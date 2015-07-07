package com.android.sunshine.app.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.android.sunshine.app.R;
import com.android.sunshine.app.location.LocationProvider;
import com.android.sunshine.app.model.OWMWeather;
import com.android.sunshine.app.model.WeatherContract;
import com.android.sunshine.app.utils.DateFormatter;
import com.android.sunshine.app.utils.TemperatureFormatter;
import javax.inject.Inject;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    private static final String[] FORECAST_COLUMNS = {
        WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID, WeatherContract.WeatherEntry.COLUMN_DATE, WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_WEATHER_DATE = 1;
    private static final int INDEX_WEATHER_CONDITION_ID = 2;
    private static final int INDEX_WEATHER_DESC = 3;
    private static final int INDEX_WEATHER_MAX_TEMP = 4;
    private static final int INDEX_WEATHER_MIN_TEMP = 5;

    @Inject
    LocationProvider locationProvider;

    @Inject
    TemperatureFormatter temperatureFormatter;

    @Inject
    DateFormatter dateFormatter;

    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationProvider.getPostCode(), System.currentTimeMillis());
                data = getContentResolver().query(weatherForLocationUri, FORECAST_COLUMNS, null, null, WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(final int position) {
                if (position == AdapterView.INVALID_POSITION
                    || data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
                int weatherId = data.getInt(INDEX_WEATHER_CONDITION_ID);
                int weatherArtResourceId = OWMWeather.getIconResourceForWeatherCondition(weatherId);
                String description = data.getString(INDEX_WEATHER_DESC);
                long dateInMillis = data.getLong(INDEX_WEATHER_DATE);
                String formattedDate = dateFormatter.getFriendlyDay(dateInMillis, false);
                double maxTemp = data.getDouble(INDEX_WEATHER_MAX_TEMP);
                double minTemp = data.getDouble(INDEX_WEATHER_MIN_TEMP);
                views.setImageViewResource(R.id.widget_icon, weatherArtResourceId);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, description);
                }
                views.setTextViewText(R.id.widget_date, formattedDate);
                views.setTextViewText(R.id.widget_description, description);
                views.setTextViewText(R.id.widget_high_temperature, temperatureFormatter.format(maxTemp));
                views.setTextViewText(R.id.widget_low_temperature, temperatureFormatter.format(minTemp));

                final Intent fillInIntent = new Intent();
                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationProvider.getPostCode(), dateInMillis);
                fillInIntent.setData(weatherUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(final RemoteViews views, final String description) {
                views.setContentDescription(R.id.widget_icon, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(final int position) {
                if (data.moveToPosition(position)) {
                    return data.getLong(INDEX_WEATHER_ID);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}

