package com.android.sunshine.app.repository;

import com.android.sunshine.app.utils.LocationProperties;
import com.android.sunshine.app.utils.Weather;
import java.util.List;

public interface ForecastRepository {

    boolean fetchForecast(String location);

    int saveWeathers(List<Weather> weathers);

    long addLocation(LocationProperties locationProperties);
}
