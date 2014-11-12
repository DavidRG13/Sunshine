package com.android.sunshine.app.fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import com.android.sunshine.app.R;
import com.android.sunshine.app.adapter.ForecastCursorAdapter;
import com.android.sunshine.app.callbacks.ItemClickCallback;
import com.android.sunshine.app.model.WeatherContract;
import com.android.sunshine.app.utils.FetchWeatherTask;
import com.android.sunshine.app.utils.Utilities;

import java.util.Date;

import static com.android.sunshine.app.model.WeatherContract.LocationEntry;
import static com.android.sunshine.app.model.WeatherContract.WeatherEntry;

public class ForecastFragment extends Fragment implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final int FORECAST_LOADER = 0;
    public static final String SCROLL_POSITION = "scrollPosition";
    private String location;
    private ForecastCursorAdapter adapter;

    private static final String[] FORECAST_COLUMNS = new String[]{
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_WEATHER_ID,
            LocationEntry.COLUMN_LOCATION_SETTING
    };
    private int scrollPosition;
    private ListView forecastList;

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
        forecastList = (ListView) view.findViewById(R.id.forecast_listview);
        forecastList.setOnItemClickListener(this);
        adapter = new ForecastCursorAdapter(getActivity(), null, 0);
        forecastList.setAdapter(adapter);
        if (savedInstanceState != null && savedInstanceState.containsKey(SCROLL_POSITION)) {
            scrollPosition = savedInstanceState.getInt(SCROLL_POSITION);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (location != null && !Utilities.getLocationSettings(getActivity()).equals(location)) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
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
    public void onSaveInstanceState(Bundle outState) {
        if (scrollPosition != ListView.INVALID_POSITION) {
            outState.putInt(SCROLL_POSITION, scrollPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        this.scrollPosition = position;
        ((ItemClickCallback) getActivity()).onItemSelected(adapter.getCursor()
                .getString(adapter.getCursor().getColumnIndex(WeatherEntry.COLUMN_DATETEXT)));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String startDate = WeatherContract.getDbDateString(new Date());

        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

        location = Utilities.getLocationSettings(getActivity());
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(location, startDate);

        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        if (scrollPosition != ListView.INVALID_POSITION) {
            forecastList.setSelection(scrollPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private void refreshWeatherData() {
        final FetchWeatherTask weatherRequester = new FetchWeatherTask(getActivity());
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        location = sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.location_default));
        weatherRequester.execute(location);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        if (adapter != null) {
            adapter.setUseTodayLayout(useTodayLayout);
        }
    }
}