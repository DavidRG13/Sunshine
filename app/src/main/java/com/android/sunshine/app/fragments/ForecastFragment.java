package com.android.sunshine.app.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.android.sunshine.app.R;
import com.android.sunshine.app.adapter.ForecastCursorAdapter;
import com.android.sunshine.app.callbacks.ItemClickCallback;
import com.android.sunshine.app.model.WeatherContract;
import com.android.sunshine.app.sync.SyncAdapter;
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
            WeatherEntry.COLUMN_WEATHER_ID,
            LocationEntry.COLUMN_LOCATION_SETTING,
            LocationEntry.COLUMN_COORD_LAT,
            LocationEntry.COLUMN_COORD_LONG
    };
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
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        forecastList = (ListView) rootView.findViewById(R.id.forecast_listview);
        forecastList.setOnItemClickListener(this);
        forecastList.setEmptyView(rootView.findViewById(R.id.empty_list));

        adapter = new ForecastCursorAdapter(getActivity(), null, 0);
        forecastList.setAdapter(adapter);
        if (savedInstanceState != null && savedInstanceState.containsKey(SCROLL_POSITION)) {
            scrollPosition = savedInstanceState.getInt(SCROLL_POSITION);
        }
        return rootView;
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

        location = Utilities.getLocationSettings(getActivity());
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(location, startDate);

        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
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
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (!Utilities.isNetworkAvailable(getActivity())) {
            ((TextView) getView().findViewById(R.id.empty_list)).setText(R.string.noWeatherInfoAvailableNoNetwork);
        } else {
            ((TextView) getView().findViewById(R.id.empty_list)).setText(R.string.noWeatherInfoAvailable);
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
            Cursor c = adapter.getCursor();
            if (null != c) {
                c.moveToPosition(0);
                String posLat = c.getString(c.getColumnIndex(LocationEntry.COLUMN_COORD_LAT));
                String posLong = c.getString(c.getColumnIndex(LocationEntry.COLUMN_COORD_LONG));
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                System.out.println("geoLocation = " + geoLocation);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                startActivity(intent);
            }
        }
    }
}