package com.android.sunshine.app.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.android.sunshine.app.R
import com.android.sunshine.app.model.WeatherContract

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

public class Utilities {
    class object {

        public fun formatTemperature(context: Context, temperature: Double): String {
            return formatTemperature(context, temperature, isMetric(context))
        }

        public fun formatTemperature(context: Context, temperature: Double, isMetric: Boolean): String {
            val temp: Double
            if (isMetric) {
                temp = temperature
            } else {
                temp = 9 * temperature / 5 + 32
            }
            return context.getString(R.string.format_temperature, temp)
        }

        public fun isMetric(context: Context): Boolean {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getString(context.getString(R.string.pref_unit_key),
                    context.getString(R.string.prefs_units_imperial)) == context.getString(R.string.prefs_units_imperial)
        }

        public fun getLocationSettings(context: Context): String {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getString(context.getString(R.string.pref_location_key), context.getString(R.string.location_default))
        }

        public fun getLastNotification(context: Context): Long {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getLong(context.getString(R.string.pref_last_notification), 0)
        }

        public fun setLastNotification(context: Context) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = prefs.edit()
            editor.putLong(context.getString(R.string.pref_last_notification), System.currentTimeMillis())
            editor.apply()
        }

        public fun displayNotifications(context: Context): Boolean {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key)
            return prefs.getBoolean(displayNotificationsKey,
                    java.lang.Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)))
        }

        public fun getFriendlyDay(context: Context, dateStr: String): String {
            val todayDate = Date()
            val todayStr = WeatherContract.getDbDateString(todayDate)
            val inputDate = WeatherContract.getDateFromDb(dateStr)

            if (todayStr == dateStr) {
                val today = context.getString(R.string.today)
                return context.getString(R.string.format_full_friendly_date, today, getFormattedMonthDay(dateStr))
            } else {
                val cal = Calendar.getInstance()
                cal.setTime(todayDate)
                cal.add(Calendar.DATE, 7)
                val weekFutureString = WeatherContract.getDbDateString(cal.getTime())

                if (dateStr.compareTo(weekFutureString) < 0) {
                    return getDayName(context, dateStr)
                } else {
                    val shortenedDateFormat = SimpleDateFormat("EEE MMM dd")
                    return shortenedDateFormat.format(inputDate)
                }
            }
        }

        public fun getDayName(context: Context, dateStr: String): String {
            val dbDateFormat = SimpleDateFormat(WeatherContract.DATE_FORMAT)
            try {
                val inputDate = dbDateFormat.parse(dateStr)
                val todayDate = Date()
                if (WeatherContract.getDbDateString(todayDate).equals(dateStr)) {
                    return context.getString(R.string.today)
                } else {
                    val cal = Calendar.getInstance()
                    cal.setTime(todayDate)
                    cal.add(Calendar.DATE, 1)
                    val tomorrowDate = cal.getTime()
                    if (WeatherContract.getDbDateString(tomorrowDate).equals(dateStr)) {
                        return context.getString(R.string.tomorrow)
                    } else {
                        val dayFormat = SimpleDateFormat("EEEE")
                        return dayFormat.format(inputDate)
                    }
                }
            } catch (e: ParseException) {
                e.printStackTrace()
                return ""
            }
        }

        public fun getFormattedMonthDay(dateStr: String): String? {
            val dbDateFormat = SimpleDateFormat(WeatherContract.DATE_FORMAT)
            try {
                val inputDate = dbDateFormat.parse(dateStr)
                val monthDayFormat = SimpleDateFormat("MMMM dd")
                return monthDayFormat.format(inputDate)
            } catch (e: ParseException) {
                e.printStackTrace()
                return null
            }
        }

        public fun getIconResourceForWeatherCondition(weatherId: Int): Int {
            if (weatherId >= 200 && weatherId <= 232) {
                return R.drawable.ic_storm
            } else if (weatherId >= 300 && weatherId <= 321) {
                return R.drawable.ic_light_rain
            } else if (weatherId >= 500 && weatherId <= 504) {
                return R.drawable.ic_rain
            } else if (weatherId == 511) {
                return R.drawable.ic_snow
            } else if (weatherId >= 520 && weatherId <= 531) {
                return R.drawable.ic_rain
            } else if (weatherId >= 600 && weatherId <= 622) {
                return R.drawable.ic_snow
            } else if (weatherId >= 701 && weatherId <= 761) {
                return R.drawable.ic_fog
            } else if (weatherId == 761 || weatherId == 781) {
                return R.drawable.ic_storm
            } else if (weatherId == 800) {
                return R.drawable.ic_clear
            } else if (weatherId == 801) {
                return R.drawable.ic_light_clouds
            } else if (weatherId >= 802 && weatherId <= 804) {
                return R.drawable.ic_cloudy
            }
            return -1
        }

        public fun getArtResourceForWeatherCondition(weatherId: Int): Int {
            if (weatherId >= 200 && weatherId <= 232) {
                return R.drawable.art_storm
            } else if (weatherId >= 300 && weatherId <= 321) {
                return R.drawable.art_light_rain
            } else if (weatherId >= 500 && weatherId <= 504) {
                return R.drawable.art_rain
            } else if (weatherId == 511) {
                return R.drawable.art_snow
            } else if (weatherId >= 520 && weatherId <= 531) {
                return R.drawable.art_rain
            } else if (weatherId >= 600 && weatherId <= 622) {
                return R.drawable.art_rain
            } else if (weatherId >= 701 && weatherId <= 761) {
                return R.drawable.art_fog
            } else if (weatherId == 761 || weatherId == 781) {
                return R.drawable.art_storm
            } else if (weatherId == 800) {
                return R.drawable.art_clear
            } else if (weatherId == 801) {
                return R.drawable.art_light_clouds
            } else if (weatherId >= 802 && weatherId <= 804) {
                return R.drawable.art_clouds
            }
            return -1
        }
    }
}