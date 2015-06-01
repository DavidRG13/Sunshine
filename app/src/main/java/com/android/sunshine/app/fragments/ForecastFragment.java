package com.android.sunshine.app.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.TextView;
import com.android.sunshine.app.R;
import com.android.sunshine.app.adapter.ForecastCursorAdapter;
import com.android.sunshine.app.adapter.OnAdapterItemClickListener;
import com.android.sunshine.app.callbacks.ItemClickCallback;
import com.android.sunshine.app.model.WeatherContract;
import com.android.sunshine.app.sync.ServerStatus;
import com.android.sunshine.app.sync.SyncAdapter;
import com.android.sunshine.app.utils.Utilities;
import java.util.Date;

import static com.android.sunshine.app.model.WeatherContract.LocationEntry;
import static com.android.sunshine.app.model.WeatherContract.WeatherEntry;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnSharedPreferenceChangeListener, OnAdapterItemClickListener {

    public static final int FORECAST_LOADER = 0;
    public static final String SCROLL_POSITION = "scrollPosition";
    private String location;
    private ForecastCursorAdapter adapter;

    private static final String[] FORECAST_COLUMNS = new String[]{
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_WEATHER_ID,
            WeatherEntry.COLUMN_WEATHER_ID,
            LocationEntry.COLUMN_LOCATION_SETTING,
            LocationEntry.COLUMN_COORD_LAT,
            LocationEntry.COLUMN_COORD_LONG
    };
    private int scrollPosition = RecyclerView.NO_POSITION;
    private RecyclerView forecastList;
    private TextView emptyView;
    private boolean autoSelectView;
    private int choiceMode;

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
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        forecastList = (RecyclerView) rootView.findViewById(R.id.recycler_view_forecast);
        forecastList.setLayoutManager(new LinearLayoutManager(getActivity()));
        forecastList.setHasFixedSize(true);
        emptyView = (TextView) rootView.findViewById(R.id.listview_forecast_empty);

        adapter = new ForecastCursorAdapter(getActivity(), emptyView, this, choiceMode);
        forecastList.setAdapter(adapter);
        if (savedInstanceState != null && savedInstanceState.containsKey(SCROLL_POSITION)) {
            scrollPosition = savedInstanceState.getInt(SCROLL_POSITION);
        }
        adapter.onRestoreInstanceState(savedInstanceState);

        final View parallaxView = rootView.findViewById(R.id.parallax_bar);
        if (null != parallaxView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                forecastList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        int max = parallaxView.getHeight();
                        if (dy > 0) {
                            parallaxView.setTranslationY(Math.max(-max, parallaxView.getTranslationY() - dy / 2));
                        } else {
                            parallaxView.setTranslationY(Math.min(0, parallaxView.getTranslationY() - dy / 2));
                        }
                    }
                });
            }
        }

        return rootView;
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.ForecastFragment, 0, 0);
        choiceMode = a.getInt(R.styleable.ForecastFragment_android_choiceMode, AbsListView.CHOICE_MODE_NONE);
        autoSelectView = a.getBoolean(R.styleable.ForecastFragment_autoSelectView, false);
        a.recycle();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.registerOnSharedPreferenceChangeListener(this);
        if (location != null && !Utilities.getLocationSettings(getActivity()).equals(location)) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.unregisterOnSharedPreferenceChangeListener(this);
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
        if (scrollPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SCROLL_POSITION, scrollPosition);
        }
        adapter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(final long date, final int position) {
        this.scrollPosition = position;
        ((ItemClickCallback) getActivity()).onItemSelected(date);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String startDate = WeatherContract.getDbDateString(new Date());

        String sortOrder = WeatherEntry.COLUMN_DATE + " ASC";

        location = Utilities.getLocationSettings(getActivity());
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(location, startDate);

        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        if (scrollPosition != RecyclerView.NO_POSITION) {
            forecastList.smoothScrollToPosition(scrollPosition);
        }
        updateEmptyView();
        if ( data.getCount() > 0 ) {
            forecastList.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (forecastList.getChildCount() > 0) {
                        forecastList.getViewTreeObserver().removeOnPreDrawListener(this);
                        int itemPosition = adapter.getSelectedItemPosition();
                        if ( RecyclerView.NO_POSITION == itemPosition ) itemPosition = 0;
                        RecyclerView.ViewHolder vh = forecastList.findViewHolderForAdapterPosition(itemPosition);
                        if ( null != vh && autoSelectView) {
                            adapter.selectView( vh );
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void updateEmptyView() {
        if (adapter.getItemCount() == 0) {
            int message = R.string.noWeatherInfoAvailable;
            @ServerStatus int status = Utilities.getServerStatus(getActivity());
            switch (status) {
                case ServerStatus.SERVER_STATUS_DOWN:
                    message = R.string.server_down;
                    break;
                case ServerStatus.SERVER_STATUS_INVALID:
                    message = R.string.server_error;
                    break;
                case ServerStatus.SERVER_STATUS_LOCATION_INVALID:
                    message = R.string.invalid_location;
                    break;
                default:
                    if (!Utilities.isNetworkAvailable(getActivity())) {
                        ((TextView) getView().findViewById(R.id.listview_forecast_empty)).setText(R.string.noWeatherInfoAvailableNoNetwork);
                    }
            }
            emptyView.setText(message);
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

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (key.equals(getString(R.string.prefs_server_status))) {
            updateEmptyView();
        }
    }
}