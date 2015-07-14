package com.android.sunshine.app.activities;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import com.android.sunshine.app.App;
import com.android.sunshine.app.R;
import com.android.sunshine.app.model.WeatherContract;
import com.android.sunshine.app.sync.WeatherRepository;
import com.android.sunshine.app.utils.ApplicationPreferences;
import com.android.sunshine.app.utils.ServerStatusChanger;
import com.android.sunshine.app.utils.TemperatureUnit;
import javax.inject.Inject;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    ServerStatusChanger serverStatusChanger;

    @Inject
    SharedPreferences preferences;

    @Inject
    ContentResolver contentResolver;

    @Inject
    WeatherRepository weatherRepository;

    @Inject
    ApplicationPreferences applicationPreferences;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((App) getApplication()).getComponent().inject(this);

        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_unit_key)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object value) {
        setPreferenceSummary(preference, value);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    private void bindPreferenceSummaryToValue(final Preference preference) {
        preference.setOnPreferenceChangeListener(this);
        setPreferenceSummary(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    private void setPreferenceSummary(final Preference preference, final Object value) {
        String stringValue = value.toString();
        String key = preference.getKey();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (key.equals(getString(R.string.pref_location_key))) {
            switch (serverStatusChanger.getServerStatus()) {
                case SERVER_STATUS_OK:
                    preference.setSummary(stringValue);
                    break;
                case SERVER_STATUS_UNKNOWN:
                    preference.setSummary(getString(R.string.prefs_location_unknown_description, value.toString()));
                    break;
                case SERVER_STATUS_INVALID:
                    preference.setSummary(getString(R.string.prefs_location_invalid_error_description, value.toString()));
                    break;
                case SERVER_STATUS_LOCATION_INVALID:
                    preference.setSummary(getString(R.string.prefs_location_invalid_error_description, value.toString()));
                    break;
                default:
                    preference.setSummary(stringValue);
            }
        } else {
            preference.setSummary(stringValue);
        }
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (key.equals(getString(R.string.pref_location_key))) {
            serverStatusChanger.resetServerStatus();
            weatherRepository.syncImmediately();
        } else if (key.equals(getString(R.string.pref_unit_key))) {
            String unit = preferences.getString(getString(R.string.pref_unit_key), getString(R.string.prefs_units_imperial));
            applicationPreferences.setTemperatureUnit(TemperatureUnit.fromString(unit));
            contentResolver.notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
        } else if (key.equals(getString(R.string.prefs_server_status))) {
            Preference locationPreference = findPreference(getString(R.string.pref_location_key));
            bindPreferenceSummaryToValue(locationPreference);
        }
    }
}
