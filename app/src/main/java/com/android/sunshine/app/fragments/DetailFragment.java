package com.android.sunshine.app.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.android.sunshine.app.R;
import com.android.sunshine.app.activities.DetailActivity;
import com.android.sunshine.app.location.LocationProvider;
import com.android.sunshine.app.location.PreferenceLocationProvider;
import com.android.sunshine.app.model.WeatherContract;
import com.android.sunshine.app.utils.DateFormatter;
import com.android.sunshine.app.utils.TemperatureFormatter;
import com.android.sunshine.app.utils.Utilities;
import java.util.Locale;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int DETAIL_LOADER = 0;
    public static final String LOCATION_KEY = "location";
    public static final String DETAIL_TRANSITION_ANIMATION = "DTA";
    public static final String DETAIL_URI = "URI";
    private static final String[] COLUMNS = new String[]{
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    @Bind(R.id.detail_icon) ImageView iconView;
    @Bind(R.id.detail_date_textview) TextView dateView;
    @Bind(R.id.detail_forecast_textview) TextView mDescriptionView;
    @Bind(R.id.detail_high_textview) TextView mHighTempView;
    @Bind(R.id.detail_low_textview) TextView mLowTempView;
    @Bind(R.id.detail_humidity_textview) TextView mHumidityView;
    @Bind(R.id.detail_wind_textview) TextView mWindView;
    @Bind(R.id.detail_pressure_textview) TextView mPressureView;

    private boolean transitionAnimation;
    private String location;
    private String weatherData;
    private LocationProvider locationProvider;
    private TemperatureFormatter temperatureFormatter;
    private DateFormatter dateFormatter;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            location = savedInstanceState.getString(LOCATION_KEY);
        }
        final Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(DetailActivity.DATE_KEY)) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            transitionAnimation = arguments.getBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, false);
        }

        final View view = inflater.inflate(R.layout.fragment_detail_start, container, false);
        ButterKnife.bind(this, view);
        locationProvider = new PreferenceLocationProvider(getActivity());
        temperatureFormatter = new TemperatureFormatter(getActivity());
        dateFormatter = new DateFormatter(getString(R.string.today), getString(R.string.tomorrow));
        return view;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (location != null) {
            outState.putString(LOCATION_KEY, location);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final Bundle arguments = getArguments();
        if (arguments != null && !location.equals(locationProvider.getLocation())
                && arguments.containsKey(DetailActivity.DATE_KEY)) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        if (getActivity() instanceof DetailActivity) {
            inflater.inflate(R.menu.detail_fragment, menu);
            finishCreatingMenu(menu);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final long date = getArguments().getLong(DetailActivity.DATE_KEY);
        location = locationProvider.getLocation();
        final Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, date);
        ViewParent vp = getView().getParent();
        if (vp instanceof CardView) {
            ((View) vp).setVisibility(View.INVISIBLE);
        }
        return new CursorLoader(getActivity(), weatherUri, COLUMNS, null, null, WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        if (data.moveToFirst()) {
            ViewParent vp = getView().getParent();
            if (vp instanceof CardView) {
                ((View) vp).setVisibility(View.VISIBLE);
            }

            final int weatherId = data.getInt(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
            final String description = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
            final long date = data.getLong(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE));
            final String wind = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED));
            final String pressure = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_PRESSURE));
            final String humidity = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY));
            final double maxTemp = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
            final double minTemp = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
            final String max = temperatureFormatter.format(maxTemp);
            final String min = temperatureFormatter.format(minTemp);

            dateView.setText(dateFormatter.getFullFriendlyDayString(date));
            mDescriptionView.setText(description);
            mHighTempView.setText(max);
            mLowTempView.setText(min);
            mHumidityView.setText(humidity);
            mWindView.setText(wind);
            mPressureView.setText(pressure);
            iconView.setImageResource(Utilities.getArtResourceForWeatherCondition(weatherId));

            weatherData = String.format(Locale.getDefault(), "%s - %s - %s/%s", date, description, max, min);

            AppCompatActivity activity = (AppCompatActivity) getActivity();
            Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);

            // We need to start the enter transition after the data has loaded
            if (transitionAnimation) {
                activity.supportStartPostponedEnterTransition();

                if (null != toolbarView) {
                    activity.setSupportActionBar(toolbarView);

                    activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                    activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            } else {
                if (null != toolbarView) {
                    Menu menu = toolbarView.getMenu();
                    if (null != menu) {
                        menu.clear();
                    }
                    toolbarView.inflateMenu(R.menu.detail_fragment);
                    finishCreatingMenu(toolbarView.getMenu());
                }
            }
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {

    }

    private void finishCreatingMenu(final Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareIntent());
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, weatherData + " #sunshine");
        return shareIntent;
    }
}
