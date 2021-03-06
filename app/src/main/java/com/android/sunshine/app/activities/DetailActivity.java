package com.android.sunshine.app.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.android.sunshine.app.App;
import com.android.sunshine.app.R;
import com.android.sunshine.app.utils.Navigator;
import com.android.sunshine.app.utils.WeatherDetails;
import javax.inject.Inject;

public class DetailActivity extends AppCompatActivity {

    @Inject
    Navigator navigator;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ((App) getApplication()).getComponent().inject(this);

        if (savedInstanceState == null) {
            final WeatherDetails weatherDetails = getIntent().getParcelableExtra(Navigator.WEATHER_DETAILS);
            navigator.detailsWithTransitionEnabled(weatherDetails, this);
        }
    }
}
