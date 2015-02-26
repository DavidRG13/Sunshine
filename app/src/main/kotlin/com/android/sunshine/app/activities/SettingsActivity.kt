package com.android.sunshine.app.activities

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import com.android.sunshine.app.R
import com.android.sunshine.app.sync.SyncAdapter

import com.android.sunshine.app.model.WeatherContract.WeatherEntry

public class SettingsActivity : PreferenceActivity(), Preference.OnPreferenceChangeListener {

    private var bindingPreference: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super<PreferenceActivity>.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_general)
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)))
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_unit_key)))
    }

    override fun onPreferenceChange(preference: Preference, value: Any): Boolean {
        val stringValue = value.toString()

        if (!bindingPreference) {
            if (preference.getKey() == getString(R.string.pref_location_key)) {
                SyncAdapter.syncImmediately(this)
            } else {
                getContentResolver().notifyChange(WeatherEntry.CONTENT_URI, null)
            }
        }

        if (preference is ListPreference) {
            val prefIndex = preference.findIndexOfValue(stringValue)
            if (prefIndex >= 0) {
                preference.setSummary(preference.getEntries()[prefIndex])
            }
        } else {
            preference.setSummary(stringValue)
        }
        return true
    }

    TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun getParentActivityIntent(): Intent? {
        return super<PreferenceActivity>.getParentActivityIntent()!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    private fun bindPreferenceSummaryToValue(preference: Preference) {
        bindingPreference = true
        preference.setOnPreferenceChangeListener(this)
        onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""))
        bindingPreference = false
    }
}