package com.android.sunshine.app.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.*;
import android.widget.TextView;
import com.android.sunshine.app.R;
import com.android.sunshine.app.utils.Utilities;

import java.util.Locale;

import static android.support.v4.app.LoaderManager.LoaderCallbacks;
import static com.android.sunshine.app.model.WeatherContract.WeatherEntry;

public class DetailActivity extends ActionBarActivity {

    public static final String DATE_KEY = "forecast_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends Fragment implements LoaderCallbacks<Cursor> {

        public static final int DETAIL_LOADER = 0;
        public static final String LOCATION_KEY = "location";
        private String location;
        private ShareActionProvider actionProvider;
        private Intent shareIntent;
        private String weatherData;
        private static final String[] COLUMNS = new String[]{
                WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
                WeatherEntry.COLUMN_DATETEXT,
                WeatherEntry.COLUMN_SHORT_DESC,
                WeatherEntry.COLUMN_MAX_TEMP,
                WeatherEntry.COLUMN_MIN_TEMP,
                WeatherEntry.COLUMN_HUMIDITY,
                WeatherEntry.COLUMN_PRESSURE,
                WeatherEntry.COLUMN_WIND_SPEED,
                WeatherEntry.COLUMN_WEATHER_ID
        };
        private String date;

        public PlaceholderFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_detail, container, false);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            if (location != null) {
                outState.putString(LOCATION_KEY, location);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if (location != null && !location.equals(Utilities.getLocationSettings(getActivity()))) {
                getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.detail_fragment, menu);
            final MenuItem menuItem = menu.findItem(R.id.menu_item_share);
            actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            if (actionProvider != null) {
                if (shareIntent == null) {
                    createShareIntent();
                }
                actionProvider.setShareIntent(shareIntent);
            }
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            date = getActivity().getIntent().getStringExtra(DATE_KEY);
            location = Utilities.getLocationSettings(getActivity());
            final Uri weatherUri = WeatherEntry.buildWeatherLocationWithDate(location, date);
            return new CursorLoader(getActivity(), weatherUri, COLUMNS, null, null, WeatherEntry.COLUMN_DATETEXT + " ASC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data.moveToFirst()) {
                final String description = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
                final String date = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_DATETEXT));
                final double maxTemp = data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP));
                final double minTemp = data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP));

                final boolean isMetric = Utilities.isMetric(getActivity());

                final TextView detailDate = (TextView) getView().findViewById(R.id.detail_date);
                final TextView detailDescription = (TextView) getView().findViewById(R.id.detail_forecast);
                final TextView detailMax = (TextView) getView().findViewById(R.id.detail_max);
                final TextView detailMin = (TextView) getView().findViewById(R.id.detail_min);
                final String max = String.valueOf(Utilities.formatTemperature(maxTemp, isMetric) + "\u00B0");
                final String min = String.valueOf(Utilities.formatTemperature(minTemp, isMetric) + "\u00B0");

                detailDate.setText(date);
                detailDescription.setText(description);
                detailMax.setText(max);
                detailMin.setText(min);

                weatherData = String.format(Locale.getDefault(), "%s - %s - %s/%s", date, description, max, min);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }

        private void createShareIntent() {
            shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, weatherData + " #sunshine");
        }
    }
}