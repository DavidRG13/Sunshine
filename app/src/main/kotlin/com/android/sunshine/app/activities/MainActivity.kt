package com.android.sunshine.app.activities

import android.support.v7.app.ActionBarActivity
import com.android.sunshine.app.callbacks.ItemClickCallback
import android.os.Bundle
import com.android.sunshine.app.fragments.ForecastFragment
import android.view.Menu
import android.view.MenuItem
import com.android.sunshine.app.R
import com.android.sunshine.app.fragments.DetailFragment
import com.android.sunshine.app.sync.SyncAdapter
import android.content.Intent

public class MainActivity : ActionBarActivity(), ItemClickCallback {

    private var twoPane: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super<ActionBarActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (findViewById(R.id.fragment_detail_container) != null) {
            twoPane = true
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_detail_container,
                        DetailFragment()).commit()
            }
        } else {
            twoPane = false
        }
        SyncAdapter.initializeSyncAdapter(this)
        val forecastFragment = getSupportFragmentManager().findFragmentById(
                R.id.fragment_forecast) as ForecastFragment
        forecastFragment.setUseTodayLayout(!twoPane)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(Intent(this, javaClass<SettingsActivity>()))
            return true
        }
        return super<ActionBarActivity>.onOptionsItemSelected(item)
    }

    override fun onItemSelected(date: String) {
        if (twoPane) {
            val args = Bundle()
            args.putString(DetailActivity().DATE_KEY, date)
            val detailFragment = DetailFragment()
            detailFragment.setArguments(args)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_detail_container, detailFragment).commitAllowingStateLoss()
        } else {
            val intent = Intent(this, javaClass<DetailActivity>())
            intent.putExtra(DetailActivity().DATE_KEY, date)
            startActivity(intent)
        }
    }
}