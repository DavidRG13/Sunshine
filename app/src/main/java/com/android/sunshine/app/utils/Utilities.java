package com.android.sunshine.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.format.Time;
import com.android.sunshine.app.R;
import com.android.sunshine.app.sync.ServerStatus;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Utilities {

    private static final String DATE_FORMAT = "yyyyMMdd";

    public static String formatTemperature(final Context context, final double temperature) {
        return formatTemperature(context, temperature, isMetric(context));
    }

    public static String formatTemperature(final Context context, final double temperature, final boolean isMetric) {
        double temp;
        if (isMetric) {
            temp = temperature;
        } else {
            temp = 9 * temperature / 5 + 32;
        }
        return context.getString(R.string.format_temperature, temp);
    }

    public static boolean isMetric(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_unit_key), context.getString(R.string.prefs_units_imperial)).equals(context.getString(R.string.prefs_units_imperial));
    }

    public static String getLocationSettings(final Context context) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(R.string.pref_location_key), context.getString(R.string.location_default));
    }

    public static long getLastNotification(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(context.getString(R.string.pref_last_notification), 0);
    }

    public static void setLastNotification(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(context.getString(R.string.pref_last_notification), System.currentTimeMillis());
        editor.apply();
    }

    public static boolean displayNotifications(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        return prefs.getBoolean(displayNotificationsKey, Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));
    }

    public static String getFriendlyDay(Context context, long dateInMillis, boolean displayLongToday) {
        Time time = new Time();
        time.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), time.gmtoff);

        if (displayLongToday && julianDay == currentJulianDay) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return context.getString(formatId, today, getFormattedMonthDay(context, dateInMillis));
        } else if ( julianDay < currentJulianDay + 7 ) {
            return getDayName(context, dateInMillis);
        } else {
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd", Locale.getDefault());
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    public static String getDayName(Context context, long dateInMillis) {
        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if (julianDay == currentJulianDay + 1) {
            return context.getString(R.string.tomorrow);
        } else {
            Time time = new Time();
            time.setToNow();
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
            return dayFormat.format(dateInMillis);
        }
    }

    public static String getFormattedMonthDay(Context context, long dateInMillis) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd", Locale.getDefault());
        return monthDayFormat.format(dateInMillis);
    }

    public static int getIconResourceForWeatherCondition(int weatherId) {
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

    public static int getArtResourceForWeatherCondition(int weatherId) {
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_rain;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }

    public static boolean isNetworkAvailable(final Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @SuppressWarnings("ResourceType")
    public static
    @ServerStatus
    int getServerStatus(final Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(context.getString(R.string.prefs_server_status), ServerStatus.SERVER_STATUS_UNKNOWN);
    }

    public static void resetServerStatus(final Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(context.getString(R.string.prefs_server_status), ServerStatus.SERVER_STATUS_UNKNOWN);
        editor.apply();
    }

    public static String getFullFriendlyDayString(Context context, long dateInMillis) {
        String day = getDayName(context, dateInMillis);
        int formatId = R.string.format_full_friendly_date;
        return context.getString(formatId, day, getFormattedMonthDay(context, dateInMillis));
    }
}