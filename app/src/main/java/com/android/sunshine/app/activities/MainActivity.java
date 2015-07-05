package com.android.sunshine.app.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.android.sunshine.app.R;
import com.android.sunshine.app.adapter.ForecastCursorAdapter;
import com.android.sunshine.app.callbacks.ItemClickCallback;
import com.android.sunshine.app.fragments.DetailFragment;
import com.android.sunshine.app.fragments.ForecastFragment;
import com.android.sunshine.app.model.WeatherContract;
import com.android.sunshine.app.sync.SyncAdapter;
import com.android.sunshine.app.utils.IntentLauncher;

public class MainActivity extends AppCompatActivity implements ItemClickCallback {

    private boolean twoPane;
    private IntentLauncher intentLauncher;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intentLauncher = new IntentLauncher();

        Uri contentUri = getIntent() != null ? getIntent().getData() : null;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (findViewById(R.id.weather_detail_container) != null) {
            twoPane = true;
            if (savedInstanceState == null) {
                DetailFragment fragment = new DetailFragment();
                if (contentUri != null) {
                    Bundle args = new Bundle();
                    args.putParcelable(DetailFragment.DETAIL_URI, contentUri);
                    fragment.setArguments(args);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment())
                        .commit();
            }
        } else {
            twoPane = false;
        }
        SyncAdapter.initializeSyncAdapter(this);
        final ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        forecastFragment.setUseTodayLayout(!twoPane);
        if (contentUri != null) {
            forecastFragment.setInitialSelectedDate(WeatherContract.WeatherEntry.getDateFromUri(contentUri));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            intentLauncher.launchSettingsActivity(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(final long date, final ForecastCursorAdapter.ViewHolder viewHolder) {
        if (twoPane) {
            final Bundle args = new Bundle();
            args.putLong(DetailActivity.DATE_KEY, date);
            final DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, detailFragment)
                    .commitAllowingStateLoss();
        } else {
            intentLauncher.transitionToDetails(this, date, viewHolder.forecastIcon);
        }
    }
}
