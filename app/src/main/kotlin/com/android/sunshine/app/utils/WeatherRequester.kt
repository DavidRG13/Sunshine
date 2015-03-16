package com.android.sunshine.app.utils

import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import kotlin.template.ToStringFormatter

public class WeatherRequester : AsyncTask<Array<String>, Void, ArrayList<String>>() {

    override fun doInBackground(vararg params: Array<String>): ArrayList<String>? {
        if (params.size() == 0) {
            return null
        }

        val builder = Uri.parse(BASE_URI).buildUpon().appendQueryParameter(QUERY_PARAM, params[0][0]).appendQueryParameter(MODE_PARAM,
                "json").appendQueryParameter(UNITS_PARAM, "metric").appendQueryParameter(DAYS_PARAM, "7")

        var urlConnection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        var forecastJsonStr: String = ""

        try {
            val url = URL(builder.build().toString())

            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection!!.setRequestMethod("GET")
            urlConnection!!.connect()

            val inputStream = urlConnection!!.getInputStream()
            val buffer = StringBuilder()
            if (inputStream != null) {
                reader = BufferedReader(InputStreamReader(inputStream))
                reader?.forEachLine { it -> buffer.append(it).append("\n") }

                if (buffer.length() > 0) {
                    forecastJsonStr = buffer.toString()
                }
                val unitType = params[0][1]
                return getWeatherDataFromJson(forecastJsonStr, unitType)
            }
        } catch (e: IOException) {
            Log.e("WeatherRequester", "Error ", e)
            return null
        } catch (e: JSONException) {
            Log.e("WeatherRequester", "Error ", e)
            return null
        } finally {
            if (urlConnection != null) {
                urlConnection!!.disconnect()
            }
            if (reader != null) {
                try {
                    reader!!.close()
                } catch (e: IOException) {
                    Log.e("PlaceholderFragment", "Error closing stream", e)
                }

            }
        }
        return null
    }

    throws(javaClass<JSONException>())
    private fun getWeatherDataFromJson(forecastJsonStr: String, unitType: String): ArrayList<String> {
        val OWM_LIST = "list"
        val OWM_WEATHER = "weather"
        val OWM_TEMPERATURE = "temp"
        val OWM_MAX = "max"
        val OWM_MIN = "min"
        val OWM_DATETIME = "dt"
        val OWM_DESCRIPTION = "main"

        val forecastJson = JSONObject(forecastJsonStr)
        val weatherArray = forecastJson.getJSONArray(OWM_LIST)

        val resultStrs = ArrayList<String>()
        for (i in 0..weatherArray.length() - 1) {
            val dayForecast = weatherArray.getJSONObject(i)
            val dateTime = dayForecast.getLong(OWM_DATETIME)
            val day = getReadableDateString(dateTime)
            val weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0)
            val description = weatherObject.getString(OWM_DESCRIPTION)
            val temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE)
            val high = temperatureObject.getDouble(OWM_MAX)
            val low = temperatureObject.getDouble(OWM_MIN)
            val highAndLow = formatHighLows(high, low, unitType)
            resultStrs.add(day + " - " + description + " - " + highAndLow)
        }
        return resultStrs
    }

    private fun getReadableDateString(time: Long): String {
        return SimpleDateFormat("E, MMM d").format(Date(time * 1000))
    }

    private fun formatHighLows(high: Double, low: Double, unitType: String): String {
        var roundedHigh= Math.round(high).toDouble()
        var roundedLow = Math.round(low).toDouble()
        if (unitType == "imperial") {
            roundedHigh *= 1.8 + 32
            roundedLow *= 1.8 + 32
        }
        return StringBuilder { append(roundedLow) append("/") append(roundedHigh)  }.toString()
    }

    class object {

        public val QUERY_PARAM: String = "q"
        public val MODE_PARAM: String = "mode"
        public val UNITS_PARAM: String = "units"
        public val DAYS_PARAM: String = "cnt"
        public val BASE_URI: String = "http://api.openweathermap.org/data/2.5/forecast/daily"
    }
}