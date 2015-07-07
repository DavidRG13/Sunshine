package com.android.sunshine.app.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
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
import butterknife.Bind;
import butterknife.ButterKnife;
import com.android.sunshine.app.App;
import com.android.sunshine.app.R;
import com.android.sunshine.app.adapter.ForecastCursorAdapter;
import com.android.sunshine.app.adapter.OnAdapterItemClickListener;
import com.android.sunshine.app.callbacks.ItemClickCallback;
import com.android.sunshine.app.location.LocationProvider;
import com.android.sunshine.app.model.WeatherContract;
import com.android.sunshine.app.sync.ServerStatus;
import com.android.sunshine.app.sync.SyncAdapter;
import com.android.sunshine.app.utils.DateFormatter;
import com.android.sunshine.app.utils.IntentLauncher;
import com.android.sunshine.app.utils.LoaderHelper;
import com.android.sunshine.app.utils.ServerStatusChanger;
import com.android.sunshine.app.utils.ServerStatusListener;
import com.android.sunshine.app.utils.TemperatureFormatter;
import javax.inject.Inject;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnAdapterItemClickListener, ServerStatusListener {

    @Bind(R.id.recycler_view_forecast) RecyclerView forecastList;
    @Bind(R.id.listview_forecast_empty) TextView emptyView;

    @Inject
    LocationProvider locationProvider;

    @Inject
    IntentLauncher intentLauncher;

    @Inject
    ServerStatusChanger serverStatusChanger;

    @Inject
    TemperatureFormatter temperatureFormatter;

    @Inject
    DateFormatter dateFormatter;

    @Inject
    SharedPreferences preferences;

    @Inject
    LoaderHelper loaderHelper;

    private ForecastCursorAdapter adapter;
    private boolean autoSelectView;
    private int choiceMode;
    private boolean holdForTransition;
    private long mInitialSelectedDate = -1;

    public ForecastFragment() {
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (holdForTransition) {
            getActivity().supportPostponeEnterTransition();
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ((App) getActivity().getApplication()).getComponent().inject(this);
        ButterKnife.bind(this, rootView);
        forecastList.setLayoutManager(new LinearLayoutManager(getActivity()));
        forecastList.setHasFixedSize(true);

        adapter = new ForecastCursorAdapter(getActivity(), emptyView, this, choiceMode, temperatureFormatter, dateFormatter);
        forecastList.setAdapter(adapter);
        if (savedInstanceState != null) {
            adapter.onRestoreInstanceState(savedInstanceState);
        }

        final View parallaxView = rootView.findViewById(R.id.parallax_bar);
        if (null != parallaxView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                forecastList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
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

        final AppBarLayout appbarView = (AppBarLayout) rootView.findViewById(R.id.appbar);
        if (null != appbarView) {
            ViewCompat.setElevation(appbarView, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                forecastList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
                        if (0 == forecastList.computeVerticalScrollOffset()) {
                            appbarView.setElevation(0);
                        } else {
                            appbarView.setElevation(appbarView.getTargetElevation());
                        }
                    }
                });
            }
        }

        return rootView;
    }

    @Override
    public void onInflate(final Activity activity, final AttributeSet attrs, final Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.ForecastFragment, 0, 0);
        choiceMode = a.getInt(R.styleable.ForecastFragment_android_choiceMode, AbsListView.CHOICE_MODE_NONE);
        autoSelectView = a.getBoolean(R.styleable.ForecastFragment_autoSelectView, false);
        holdForTransition = a.getBoolean(R.styleable.ForecastFragment_sharedElementTransitions, false);
        a.recycle();
    }

    @Override
    public void onResume() {
        super.onResume();
        serverStatusChanger.addListener(this);
        getLoaderManager().initLoader(loaderHelper.getForecastLoaderId(), null, this);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
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
    public void onSaveInstanceState(final Bundle outState) {
        adapter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(final long date, final ForecastCursorAdapter.ViewHolder viewHolder) {
        ((ItemClickCallback) getActivity()).onItemSelected(date, viewHolder);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        return loaderHelper.getForecastCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        adapter.swapCursor(data);
        updateEmptyView();
        if (data.getCount() == 0) {
            getActivity().supportStartPostponedEnterTransition();
        } else {
            forecastList.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (forecastList.getChildCount() > 0) {
                        forecastList.getViewTreeObserver().removeOnPreDrawListener(this);
                        int position = adapter.getSelectedItemPosition();
                        if (position == RecyclerView.NO_POSITION && -1 != mInitialSelectedDate) {
                            Cursor data = adapter.getCursor();
                            int count = data.getCount();
                            int dateColumn = data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
                            for (int i = 0; i < count; i++) {
                                data.moveToPosition(i);
                                if (data.getLong(dateColumn) == mInitialSelectedDate) {
                                    position = i;
                                    break;
                                }
                            }
                        }
                        if (position == RecyclerView.NO_POSITION) position = 0;
                        // If we don't need to restart the loader, and there's a desired position to restore
                        // to, do so now.
                        forecastList.smoothScrollToPosition(position);
                        RecyclerView.ViewHolder vh = forecastList.findViewHolderForAdapterPosition(position);
                        if (null != vh && autoSelectView) {
                            adapter.selectView(vh);
                        }
                        if (holdForTransition) {
                            getActivity().supportStartPostponedEnterTransition();
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onServerStatusChanged(final ServerStatus status) {
        updateEmptyView();
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private void updateEmptyView() {
        if (adapter.getItemCount() == 0) {
            emptyView.setText(serverStatusChanger.getServerStatus().getMessageResource());
        }
    }

    public void setUseTodayLayout(final boolean useTodayLayout) {
        if (adapter != null) {
            adapter.setUseTodayLayout(useTodayLayout);
        }
    }

    private void refreshWeatherData() {
        SyncAdapter.syncImmediately(getActivity());
    }

    public void setInitialSelectedDate(final long initialSelectedDate) {
        mInitialSelectedDate = initialSelectedDate;
    }

    private void showCurrentLocation() {
        if (null != adapter) {
            intentLauncher.displayMapWithLocation(getActivity(), locationProvider.getLocation());
        }
    }
}