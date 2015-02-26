package com.android.sunshine.app.model

import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

public class WeatherContract {

    public class WeatherEntry : BaseColumns {
        class object {

            public val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build()

            public val CONTENT_TYPE: String = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER
            public val CONTENT_ITEM_TYPE: String = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER
            public val TABLE_NAME: String = "weather"

            public val COLUMN_LOC_KEY: String = "location_id"
            public val COLUMN_DATETEXT: String = "date"
            public val COLUMN_WEATHER_ID: String = "weather_id"
            public val COLUMN_SHORT_DESC: String = "short_desc"
            public val COLUMN_MIN_TEMP: String = "min"
            public val COLUMN_MAX_TEMP: String = "max"
            public val COLUMN_HUMIDITY: String = "humidity"
            public val COLUMN_PRESSURE: String = "pressure"
            public val COLUMN_WIND_SPEED: String = "wind"
            public val COLUMN_DEGREES: String = "degrees"

            public fun buildWeatherUri(id: Long): Uri {
                return ContentUris.withAppendedId(CONTENT_URI, id)
            }

            public fun buildWeatherLocation(locationSetting: String): Uri {
                return CONTENT_URI.buildUpon().appendPath(locationSetting).build()
            }

            public fun buildWeatherLocationWithStartDate(locationSetting: String, startDate: String): Uri {
                return CONTENT_URI.buildUpon().appendPath(locationSetting).appendQueryParameter(COLUMN_DATETEXT, startDate).build()
            }

            public fun buildWeatherLocationWithDate(locationSetting: String, date: String): Uri {
                return CONTENT_URI.buildUpon().appendPath(locationSetting).appendPath(date).build()
            }

            public fun getLocationSettingFromUri(uri: Uri): String {
                return uri.getPathSegments().get(1)
            }

            public fun getDateFromUri(uri: Uri): String {
                return uri.getPathSegments().get(2)
            }

            public fun getStartDateFromUri(uri: Uri): String {
                return uri.getQueryParameter(COLUMN_DATETEXT)
            }
        }
    }

    public class LocationEntry : BaseColumns {
        class object {

            public val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build()
            public val CONTENT_TYPE: String = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION
            public val CONTENT_ITEM_TYPE: String = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION
            public val TABLE_NAME: String = "location"

            public val COLUMN_LOCATION_SETTING: String = "location_setting"
            public val COLUMN_CITY_NAME: String = "city_name"
            public val COLUMN_COORD_LAT: String = "coord_lat"
            public val COLUMN_COORD_LONG: String = "coord_long"
            public fun buildLocationUri(id: Long): Uri {
                return ContentUris.withAppendedId(CONTENT_URI, id)
            }
        }
    }

    class object {

        public val CONTENT_AUTHORITY: String = "com.android.sunshine.app"
        public val BASE_CONTENT_URI: Uri = Uri.parse("content://" + CONTENT_AUTHORITY)
        public val PATH_WEATHER: String = "weather"
        public val PATH_LOCATION: String = "location"
        public val DATE_FORMAT: String = "yyyyMMdd"

        public fun getDbDateString(date: Date): String {
            val dateFormat = SimpleDateFormat(DATE_FORMAT)
            return dateFormat.format(date)
        }

        public fun getDateFromDb(date: String): Date? {
            val simpleDateFormat = SimpleDateFormat(DATE_FORMAT)
            try {
                return simpleDateFormat.parse(date)
            } catch (e: ParseException) {
                e.printStackTrace()
                return null;
            }
        }
    }
}