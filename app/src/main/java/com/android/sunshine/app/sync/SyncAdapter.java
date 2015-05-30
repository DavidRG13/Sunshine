package com.android.sunshine.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import com.android.sunshine.app.R;
import com.android.sunshine.app.activities.MainActivity;
import com.android.sunshine.app.model.OWMResponse;
import com.android.sunshine.app.model.OWMWeatherForecast;
import com.android.sunshine.app.model.WeatherContract;
import com.android.sunshine.app.utils.Utilities;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import static com.android.sunshine.app.model.WeatherContract.WeatherEntry;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SERVER_STATUS_OK, SERVER_STATUS_DOWN, SERVER_STATUS_INVALID, SERVER_STATUS_UNKNOWN})
    public @interface ServerStatus{}
    public static final int SERVER_STATUS_OK = 0;
    public static final int SERVER_STATUS_DOWN = 1;
    public static final int SERVER_STATUS_INVALID = 2;
    public static final int SERVER_STATUS_UNKNOWN = 3;

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    public static final String QUERY_PARAM = "q";
    public static final String MODE_PARAM = "mode";
    public static final String UNITS_PARAM = "units";
    public static final String DAYS_PARAM = "cnt";
    public static final String BASE_URI = "http://api.openweathermap.org/data/2.5/forecast/daily";

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;

    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[]{
            WeatherEntry.COLUMN_WEATHER_ID,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_SHORT_DESC
    };

    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        final String locationSettings = Utilities.getLocationSettings(getContext());

        Uri.Builder builder = Uri.parse(BASE_URI).buildUpon()
                .appendQueryParameter(QUERY_PARAM, locationSettings)
                .appendQueryParameter(MODE_PARAM, "json")
                .appendQueryParameter(UNITS_PARAM, "metric")
                .appendQueryParameter(DAYS_PARAM, "14");

        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(builder.build().toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            ObjectMapper mapper = new ObjectMapper();
            OWMResponse owmResponse = mapper.readValue(urlConnection.getInputStream(), OWMResponse.class);
            parseWeatherDataFromJson(owmResponse, locationSettings);
        } catch (IOException e) {
            Log.e("WeatherRequester", "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(syncInterval, flexTime).setSyncAdapter(account, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if (null == accountManager.getPassword(newAccount)) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private void parseWeatherDataFromJson(OWMResponse owmResponse, String locationSettings) {
        String cityName = owmResponse.getCity().getName();
        double cityLatitude = owmResponse.getCity().getCoord().getLat();
        double cityLongitude = owmResponse.getCity().getCoord().getLon();

        long locationId = addLocation(locationSettings, cityName, cityLatitude, cityLongitude);

        ArrayList<OWMWeatherForecast> weatherForecasts = owmResponse.getList();
        Vector<ContentValues> cVVector = new Vector<>(weatherForecasts.size());
        for (OWMWeatherForecast weatherForecast : weatherForecasts) {
            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValues.put(WeatherEntry.COLUMN_DATETEXT, WeatherContract.getDbDateString(new Date(weatherForecast.getDt() * 1000L)));
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, weatherForecast.getHumidity());
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, weatherForecast.getPressure());
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, weatherForecast.getSpeed());
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, weatherForecast.getDeg());
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, weatherForecast.getTemp().getMax());
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, weatherForecast.getTemp().getMin());
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, weatherForecast.getWeather().get(0).getDescription());
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherForecast.getWeather().get(0).getId());

            cVVector.add(weatherValues);
        }

        if (cVVector.size() > 0) {
            ContentValues[] contentValues = new ContentValues[cVVector.size()];
            cVVector.toArray(contentValues);
            getContext().getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI, contentValues);
            notifyWeather();

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterdayDate = WeatherContract.getDbDateString(cal.getTime());
            getContext().getContentResolver().delete(WeatherEntry.CONTENT_URI, WeatherEntry.COLUMN_DATETEXT + " <= ?", new String[] {yesterdayDate});
        }
    }

    private long addLocation(final String locationSettings, final String cityName, final double cityLatitude, final double cityLongitude) {
        ContentValues locationValues = new ContentValues();
        locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSettings);
        locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
        locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, cityLatitude);
        locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, cityLongitude);

        final Cursor cursor = getContext().getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI, new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?", new String[]{locationSettings}, null);
        if (cursor.moveToFirst()) {
            return cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
        } else {
            return ContentUris.parseId(getContext().getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, locationValues));
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    private void notifyWeather() {
        if (Utilities.displayNotifications(getContext())) {
            long lastSync = Utilities.getLastNotification(getContext());

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                String locationQuery = Utilities.getLocationSettings(getContext());

                Uri weatherUri = WeatherEntry.buildWeatherLocationWithDate(locationQuery, WeatherContract.getDbDateString(new Date()));

                Cursor cursor = getContext().getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

                if (cursor.moveToFirst()) {
                    int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                    double high = cursor.getDouble(INDEX_MAX_TEMP);
                    double low = cursor.getDouble(INDEX_MIN_TEMP);
                    String desc = cursor.getString(INDEX_SHORT_DESC);

                    int iconId = Utilities.getIconResourceForWeatherCondition(weatherId);
                    String title = getContext().getString(R.string.app_name);

                    String contentText = String.format(getContext().getString(R.string.format_notification),
                            desc,
                            Utilities.formatTemperature(getContext(), high),
                            Utilities.formatTemperature(getContext(), low));

                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext())
                            .setSmallIcon(iconId)
                            .setContentTitle(title)
                            .setContentText(contentText);

                    Intent resultIntent = new Intent(getContext(), MainActivity.class);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());

                    Utilities.setLastNotification(getContext());
                }
            }
        }
    }
}