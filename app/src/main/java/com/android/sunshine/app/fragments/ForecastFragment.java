package com.android.sunshine.app.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.android.sunshine.app.R;
import com.android.sunshine.app.activities.DetailActivity;
import com.android.sunshine.app.model.WeatherContract;
import com.android.sunshine.app.utils.FetchWeatherTask;
import com.android.sunshine.app.utils.Utilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.android.sunshine.app.model.WeatherContract.LocationEntry;
import static com.android.sunshine.app.model.WeatherContract.WeatherEntry;

public class ForecastFragment extends Fragment implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final int FORECAST_LOADER = 0;
    private SimpleCursorAdapter adapter;

    private static final String[] FORECAST_COLUMNS = new String[]{
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            LocationEntry.COLUMN_LOCATION_SETTING
    };

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;

    public ForecastFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View view = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView forecastList = (ListView) view.findViewById(R.id.forecast_listview);
        forecastList.setOnItemClickListener(this);
        final List<String> adapterData = new ArrayList<>();
        adapter = new SimpleCursorAdapter(getActivity(), R.layout.forecast_list_item, null, new String[]{
                WeatherEntry.COLUMN_DATETEXT,
                WeatherEntry.COLUMN_SHORT_DESC,
                WeatherEntry.COLUMN_MAX_TEMP,
                WeatherEntry.COLUMN_MIN_TEMP
        }, new int[]{
                R.id.list_item_date,
                R.id.list_item_forecast,
                R.id.list_item_max,
                R.id.list_item_min
        }, 0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                boolean isMetric = Utilities.isMetric(getActivity());
                switch (columnIndex) {
                    case COL_WEATHER_MAX_TEMP:
                    case COL_WEATHER_MIN_TEMP:
                        ((TextView) view).setText(Utilities.formatTemperature(cursor.getDouble(columnIndex), isMetric));
                        break;
                    case COL_WEATHER_DATE:
                        final String date = cursor.getString(columnIndex);
                        ((TextView) view).setText(Utilities.formatDate(date));
                        break;
                }
                return false;
            }
        });

        forecastList.setAdapter(adapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshWeatherData();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            refreshWeatherData();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Intent intent = new Intent(getActivity(), DetailActivity.class);
        final Cursor cursor = adapter.getCursor();
        String forecast = "";
        if (cursor != null && cursor.moveToPosition(position)) {
            boolean isMetric = Utilities.isMetric(getActivity());
            forecast = String.format(Locale.getDefault(), "%s - %s - %s/%s",
                    Utilities.formatDate(cursor.getString(COL_WEATHER_DATE)),
                    cursor.getString(COL_WEATHER_DESC),
                    Utilities.formatTemperature(cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric),
                    Utilities.formatTemperature(cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric));
        }
        intent.putExtra(DetailActivity.PlaceholderFragment.WEATHER_DATA, forecast);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String startDate = WeatherContract.getDbDateString(new Date());

        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String location = sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.location_default));
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(location, startDate);

        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private void refreshWeatherData() {
        final FetchWeatherTask weatherRequester = new FetchWeatherTask(getActivity());
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String location = sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.location_default));
        final String unit = sharedPreferences.getString(getString(R.string.pref_unit_key), getString(R.string.prefs_units_imperial));
        weatherRequester.execute(new String[]{location, unit});
    }
}