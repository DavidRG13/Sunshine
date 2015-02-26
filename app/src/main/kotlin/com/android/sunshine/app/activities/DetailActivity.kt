package com.android.sunshine.app.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.view.Menu
import android.view.MenuItem
import com.android.sunshine.app.R
import com.android.sunshine.app.fragments.DetailFragment

public class DetailActivity : ActionBarActivity() {

    public val DATE_KEY: String = "forecast_date"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        if (savedInstanceState == null) {
            val date = getIntent().getStringExtra(DATE_KEY)
            val bundle = Bundle()
            bundle.putString(DATE_KEY, date)
            val detailFragment = DetailFragment()
            detailFragment.setArguments(bundle)
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_detail_container, detailFragment).commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()
        if (id == R.id.action_settings) {
            startActivity(Intent(this, javaClass<SettingsActivity>()))
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}