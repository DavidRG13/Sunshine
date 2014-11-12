package com.android.sunshine.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import com.android.sunshine.app.R;
import com.android.sunshine.app.activities.MainActivity;
import com.android.sunshine.app.model.WeatherContract;
import com.android.sunshine.app.utils.Utilities;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import static com.android.sunshine.app.model.WeatherContract.WeatherEntry;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

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
        BufferedReader reader = null;
        String forecastJsonStr = null;

        try {
            final String uri = builder.build().toString();
            URL url = new URL(uri);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream != null) {
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() > 0) {
                    forecastJsonStr = buffer.toString();
                }
                parseWeatherDataFromJson(forecastJsonStr, locationSettings);
            }
        } catch (IOException | JSONException e) {
            Log.e("WeatherRequester", "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
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

    private void parseWeatherDataFromJson(String forecastJsonStr, String locationSettings) throws JSONException {
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DATETIME = "dt";
        final String OWM_DESCRIPTION = "main";
        final String OWM_LAT = "lon";
        final String OWM_LNG = "lat";
        final String OWM_CITY = "city";
        final String OWM_NAME = "name";
        final String OWM_COORDS = "coord";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";
        final String OWM_WEATHER_ID = "id";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
        final JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        final String cityName = cityJson.getString(OWM_NAME);
        final JSONObject coordsJson = cityJson.getJSONObject(OWM_COORDS);
        final double cityLatitude = coordsJson.getDouble(OWM_LAT);
        final double cityLongitude = coordsJson.getDouble(OWM_LNG);

        final long locationId = addLocation(locationSettings, cityName, cityLatitude, cityLongitude);

        Vector<ContentValues> cVVector = new Vector<>(weatherArray.length());
        for (int i = 0; i < weatherArray.length(); i++) {
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            long dateTime = dayForecast.getLong(OWM_DATETIME);
            final double pressure = dayForecast.getDouble(OWM_PRESSURE);
            final int humidity = dayForecast.getInt(OWM_HUMIDITY);
            final double windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            final double windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            final String description = weatherObject.getString(OWM_DESCRIPTION);
            final int weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValues.put(WeatherEntry.COLUMN_DATETEXT, WeatherContract.getDbDateString(new Date(dateTime * 1000L)));
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

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
            getContext().getContentResolver().delete(WeatherEntry.CONTENT_URI,
                    WeatherEntry.COLUMN_DATETEXT + " <= ?", new String[] {yesterdayDate});
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