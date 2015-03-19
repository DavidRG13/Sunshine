package com.android.sunshine.app.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import com.android.sunshine.app.R;
import com.android.sunshine.app.SunshineApplication;
import com.android.sunshine.app.utils.DateFormatter;
import com.android.sunshine.app.adapter.ForecastCursorAdapter;
import com.android.sunshine.app.callbacks.ItemClickCallback;
import com.android.sunshine.app.repository.WeatherContract;
import com.android.sunshine.app.repository.PreferenceRepository;
import com.android.sunshine.app.sync.SyncAdapter;

import com.android.sunshine.app.utils.TemperatureFormatter;
import com.android.sunshine.app.utils.WeatherImageProvider;
import java.util.Date;
import javax.inject.Inject;

import static com.android.sunshine.app.repository.WeatherContract.LocationEntry;
import static com.android.sunshine.app.repository.WeatherContract.WeatherEntry;

public class ForecastFragment extends Fragment implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    @Inject
    PreferenceRepository preferenceRepository;

    @Inject
    DateFormatter dateFormatter;

    @Inject
    TemperatureFormatter temperatureFormatter;

    @Inject
    WeatherImageProvider weatherImageProvider;

    public static final int FORECAST_LOADER = 0;
    public static final String SCROLL_POSITION = "scrollPosition";
    private String location;
    private ForecastCursorAdapter adapter;
    private int scrollPosition;
    private ListView forecastList;
    private View rootView;

    public ForecastFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((SunshineApplication) getActivity().getApplication()).getObjectGraph().inject(this);
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        forecastList = (ListView) rootView.findViewById(R.id.forecast_listview);
        forecastList.setOnItemClickListener(this);
        adapter = new ForecastCursorAdapter(getActivity(), null, 0, dateFormatter, temperatureFormatter, preferenceRepository.isMetric(), weatherImageProvider);
        forecastList.setAdapter(adapter);
        if (savedInstanceState != null && savedInstanceState.containsKey(SCROLL_POSITION)) {
            scrollPosition = savedInstanceState.getInt(SCROLL_POSITION);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (location != null && ! preferenceRepository.getLocation().equals(location)) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.action_refresh) {
            refreshWeatherData();
        } else if (itemId == R.id.viewLocation) {
            showCurrentLocation();
            return true;
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
        ((ItemClickCallback) getActivity()).onItemSelected(adapter.getCursor().getString(adapter.getCursor().getColumnIndex(WeatherEntry.COLUMN_DATETEXT)));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String startDate = WeatherContract.getDbDateString(new Date());
        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";
        location = preferenceRepository.getLocation();
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(location, startDate);

        return new CursorLoader(getActivity(), weatherForLocationUri, WeatherEntry.FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        if (scrollPosition != ListView.INVALID_POSITION) {
            forecastList.smoothScrollToPosition(scrollPosition);
            if (!adapter.getUseTodayLayout()) {
                forecastList.performItemClick(rootView, scrollPosition, forecastList.getAdapter().getItemId(scrollPosition));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        if (adapter != null) {
            adapter.setUseTodayLayout(useTodayLayout);
        }
    }

    private void refreshWeatherData() {
        SyncAdapter.syncImmediately(getActivity());
    }

    private void showCurrentLocation() {
        if (null != adapter) {
            Cursor cursor = adapter.getCursor();
            if (null != cursor) {
                cursor.moveToPosition(0);
                String posLat = cursor.getString(cursor.getColumnIndex(LocationEntry.COLUMN_COORD_LAT));
                String posLong = cursor.getString(cursor.getColumnIndex(LocationEntry.COLUMN_COORD_LONG));
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                startActivity(intent);
            }
        }
    }
}