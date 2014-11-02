package com.android.sunshine.app.utils;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherRequester extends AsyncTask<String, Void, Void> {

    public static final String QUERY_PARAM = "q";
    public static final String MODE_PARAM = "mode";
    public static final String UNITS_PARAM = "units";
    public static final String DAYS_PARAM = "cnt";
    public static final String BASE_URI = "http://api.openweathermap.org/data/2.5/forecast/daily";

    @Override
    protected Void doInBackground(String... params) {
        if(params.length == 0){
            return null;
        }

        Uri.Builder builder =Uri.parse(BASE_URI).buildUpon()
                .appendQueryParameter(QUERY_PARAM, params[0])
                .appendQueryParameter(MODE_PARAM, "json")
                .appendQueryParameter(UNITS_PARAM, "metric")
                .appendQueryParameter(DAYS_PARAM, "7");

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String forecastJsonStr = null;

        try {
            final String uri = builder.build().toString();
            Log.v("URIIIIII  ", uri);
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
                Log.v("forecastJsonStr", forecastJsonStr);
            }
        } catch (IOException e) {
            Log.e("PlaceholderFragment", "Error ", e);
            forecastJsonStr = null;
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
        return null;
    }
}