package com.android.sunshine.app.repository;

import com.android.sunshine.app.utils.LocationProperties;
import com.android.sunshine.app.utils.Weather;
import java.util.List;

public interface ForecastRepository {

    void saveWeathers(List<Weather> weathers);

    long addLocation(LocationProperties locationProperties);
}
