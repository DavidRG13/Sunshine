package com.android.sunshine.app.weather;

import com.android.sunshine.app.model.OWMResponse;
import retrofit.http.GET;
import retrofit.http.Query;

public interface OWM {

    String API_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
    String QUERY_PARAM = "q";
    String MODE_PARAM = "mode";
    String UNITS_PARAM = "units";
    String DAYS_PARAM = "cnt";

    @GET("/")
    OWMResponse fetch(@Query(QUERY_PARAM) String queryParam, @Query(MODE_PARAM) String mode, @Query(UNITS_PARAM) String units, @Query(DAYS_PARAM) String days);
}
