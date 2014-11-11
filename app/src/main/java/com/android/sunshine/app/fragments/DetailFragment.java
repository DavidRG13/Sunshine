package com.android.sunshine.app.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.*;
import android.widget.TextView;
import com.android.sunshine.app.R;
import com.android.sunshine.app.activities.DetailActivity;
import com.android.sunshine.app.model.WeatherContract;
import com.android.sunshine.app.utils.Utilities;

import java.util.Locale;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int DETAIL_LOADER = 0;
    public static final String LOCATION_KEY = "location";
    private String location;
    private Intent shareIntent;
    private String weatherData;
    private static final String[] COLUMNS = new String[]{
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };
    private String date;

    public DetailFragment() {
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
        final ShareActionProvider actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (actionProvider != null) {
            if (shareIntent == null) {
                createShareIntent();
            }
            actionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        date = getActivity().getIntent().getStringExtra(DetailActivity.DATE_KEY);
        location = Utilities.getLocationSettings(getActivity());
        final Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, date);
        return new CursorLoader(getActivity(), weatherUri, COLUMNS, null, null, WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            final String description = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
            final String date = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT));
            final double maxTemp = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
            final double minTemp = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));

            final boolean isMetric = Utilities.isMetric(getActivity());

            final TextView detailDate = (TextView) getView().findViewById(R.id.detail_date);
            final TextView detailDescription = (TextView) getView().findViewById(R.id.detail_forecast);
            final TextView detailMax = (TextView) getView().findViewById(R.id.detail_max);
            final TextView detailMin = (TextView) getView().findViewById(R.id.detail_min);
            final String max = String.valueOf(Utilities.formatTemperature(getActivity(), maxTemp, isMetric));
            final String min = String.valueOf(Utilities.formatTemperature(getActivity(), minTemp, isMetric));

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
