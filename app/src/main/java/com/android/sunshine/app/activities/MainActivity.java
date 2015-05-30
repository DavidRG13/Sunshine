package com.android.sunshine.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.android.sunshine.app.R;
import com.android.sunshine.app.callbacks.ItemClickCallback;
import com.android.sunshine.app.fragments.DetailFragment;
import com.android.sunshine.app.fragments.ForecastFragment;
import com.android.sunshine.app.sync.SyncAdapter;

public class MainActivity extends AppCompatActivity implements ItemClickCallback {

    private boolean twoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (findViewById(R.id.fragment_detail_container) != null) {
            twoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_detail_container, new DetailFragment())
                        .commit();
            }
        } else {
            twoPane = false;
        }
        SyncAdapter.initializeSyncAdapter(this);
        final ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        forecastFragment.setUseTodayLayout(!twoPane);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(String date) {
        if (twoPane) {
            final Bundle args = new Bundle();
            args.putString(DetailActivity.DATE_KEY, date);
            final DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_detail_container, detailFragment)
                    .commitAllowingStateLoss();
        } else {
            final Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.DATE_KEY, date);
            startActivity(intent);
        }
    }
}