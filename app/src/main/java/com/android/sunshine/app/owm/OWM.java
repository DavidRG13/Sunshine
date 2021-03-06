package com.android.sunshine.app.owm;

import com.android.sunshine.app.owm.model.OWMResponse;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface OWM {

    String API_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
    String QUERY_PARAM = "q";
    String MODE_PARAM = "mode";
    String UNITS_PARAM = "units";
    String DAYS_PARAM = "cnt";

    @GET("/")
    void fetch(@Query(QUERY_PARAM) String queryParam, @Query(MODE_PARAM) String mode, @Query(UNITS_PARAM) String units, @Query(DAYS_PARAM) String days, final Callback<OWMResponse> callback);
}
