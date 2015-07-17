package com.android.sunshine.app.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.android.sunshine.app.App;
import com.android.sunshine.app.R;
import com.android.sunshine.app.adapter.ForecastCursorAdapter;
import com.android.sunshine.app.adapter.ItemClickCallback;
import com.android.sunshine.app.utils.ApplicationPreferences;
import com.android.sunshine.app.utils.Navigator;
import com.android.sunshine.app.utils.WeatherDetails;
import com.android.sunshine.app.weather.WeatherRepository;
import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements ItemClickCallback {

    @Inject
    Navigator navigator;

    @Inject
    ApplicationPreferences applicationPreferences;

    @Inject
    WeatherRepository weatherRepository;

    private boolean twoPane;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((App) getApplication()).getComponent().inject(this);

        long dateFromUri = getIntent().getLongExtra("date", 0);
        if (dateFromUri != 0) {
            applicationPreferences.setInitialSelectedDate(dateFromUri);
            // TODO: mirar para k usamos InitialSelectedDate
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (findViewById(R.id.weather_detail_container) != null) {
            twoPane = true;
            if (savedInstanceState == null) {
                navigator.twoPaneDetails(weatherRepository.getForecastForDetailWidget().get(0), this);
            }
        } else {
            twoPane = false;
        }
        applicationPreferences.useTodayLayout(!twoPane);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            navigator.launchSettingsActivity(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(final WeatherDetails weatherDetails, final ForecastCursorAdapter.ViewHolder viewHolder) {
        if (twoPane) {
            navigator.twoPaneDetails(weatherDetails, this);
        } else {
            navigator.transitionToDetails(this, weatherDetails, viewHolder.forecastIcon);
        }
    }
}
