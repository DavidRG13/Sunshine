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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.sunshine.app.R;
import com.android.sunshine.app.SunshineApplication;
import com.android.sunshine.app.activities.DetailActivity;
import com.android.sunshine.app.utils.DateFormatter;
import com.android.sunshine.app.repository.WeatherContract.WeatherEntry;
import com.android.sunshine.app.repository.PreferenceRepository;
import com.android.sunshine.app.utils.TemperatureFormatter;
import com.android.sunshine.app.utils.Utilities;
import java.util.Locale;
import javax.inject.Inject;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    @Inject
    PreferenceRepository preferenceRepository;

    @Inject
    DateFormatter dateFormatter;

    @Inject
    TemperatureFormatter temperatureFormatter;

    public static final int DETAIL_LOADER = 0;
    public static final String LOCATION_KEY = "location";
    private String location;
    private Intent shareIntent;
    private String weatherData;
    private TextView detailDate;
    private TextView detailDescription;
    private TextView detailMax;
    private TextView detailMin;
    private TextView detailDay;
    private TextView detailWind;
    private TextView detailPressure;
    private TextView detailHumidity;
    private ImageView detailIcon;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((SunshineApplication) getActivity().getApplication()).getObjectGraph().inject(this);

        if(savedInstanceState != null) {
            location = savedInstanceState.getString(LOCATION_KEY);
        }
        final Bundle arguments = getArguments();
        if(arguments != null && arguments.containsKey(DetailActivity.DATE_KEY)) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_detail, container, false);
        detailDate = (TextView) view.findViewById(R.id.detail_date);
        detailDescription = (TextView) view.findViewById(R.id.detail_forecast);
        detailMax = (TextView) view.findViewById(R.id.detail_max);
        detailMin = (TextView) view.findViewById(R.id.detail_min);
        detailDay = (TextView) view.findViewById(R.id.detail_day);
        detailWind = (TextView) view.findViewById(R.id.detail_wind);
        detailPressure = (TextView) view.findViewById(R.id.detail_pressure);
        detailHumidity = (TextView) view.findViewById(R.id.detail_humidity);
        detailIcon = (ImageView) view.findViewById(R.id.detail_icon);
        return view;
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
        final Bundle arguments = getArguments();
        if (arguments != null && !location.equals(preferenceRepository.getLocation())
                && arguments.containsKey(DetailActivity.DATE_KEY)) {
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
        final String date = getArguments().getString(DetailActivity.DATE_KEY);
        location = preferenceRepository.getLocation();
        final Uri weatherUri = WeatherEntry.buildWeatherLocationWithDate(location, date);
        return new CursorLoader(getActivity(), weatherUri, WeatherEntry.DETAIL_COLUMNS, null, null, WeatherEntry.COLUMN_DATETEXT + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            final int weatherId = data.getInt(data.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID));
            final String description = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
            final String date = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_DATETEXT));
            final String wind = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED));
            final String pressure = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_PRESSURE));
            final String humidity = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY));
            final double maxTemp = data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP));
            final double minTemp = data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP));
            final String max = String.valueOf(temperatureFormatter.format(maxTemp, preferenceRepository.isMetric()));
            final String min = String.valueOf(temperatureFormatter.format(minTemp, preferenceRepository.isMetric()));

            detailDate.setText(date);
            detailDescription.setText(description);
            detailMax.setText(max);
            detailMin.setText(min);
            detailHumidity.setText(humidity);
            detailWind.setText(wind);
            detailPressure.setText(pressure);
            detailDay.setText(dateFormatter.getDayName(date));
            detailIcon.setImageResource(Utilities.getArtResourceForWeatherCondition(weatherId));

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