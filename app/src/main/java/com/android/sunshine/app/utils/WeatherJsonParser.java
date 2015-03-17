package com.android.sunshine.app.utils;

import com.android.sunshine.app.utils.LocationProperties;
import com.android.sunshine.app.utils.Weather;
import java.util.ArrayList;
import org.json.JSONException;

public interface WeatherJsonParser {

    ArrayList<Weather> parseWeatherDataFromJson(String forecastJsonStr, long locationId);

    LocationProperties parseLocation(String forecastJsonStr, String locationSettings);
}
