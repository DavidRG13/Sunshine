package com.android.sunshine.app.fragments;

import android.database.Cursor;
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
import com.android.sunshine.app.R;
import com.android.sunshine.app.activities.MainActivity;
import com.android.sunshine.app.adapter.ForecastCursorAdapter;
import com.android.sunshine.app.callbacks.ItemClickCallback;
import com.android.sunshine.app.sync.SyncAdapter;

import static com.android.sunshine.app.model.Contract.ArticleEntry;

public class ForecastFragment extends Fragment implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final int FORECAST_LOADER = 0;
    public static final String SCROLL_POSITION = "scrollPosition";
    private ForecastCursorAdapter adapter;

    private static final String[] FORECAST_COLUMNS = new String[]{
            ArticleEntry.TABLE_NAME + "." + ArticleEntry._ID,
            ArticleEntry.COLUMN_URL,
            ArticleEntry.COLUMN_SNIPPET,
            ArticleEntry.COLUMN_DATE,
            ArticleEntry.COLUMN_SECTION_NAME,
            ArticleEntry.COLUMN_SHORT_DESCRIPTION,
            ArticleEntry.COLUMN_THUMBNAIL,
            ArticleEntry.COLUMN_LARGE_IMAGE
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
        adapter = new ForecastCursorAdapter(getActivity(), null, 0);
        forecastList.setAdapter(adapter);
        if (savedInstanceState != null && savedInstanceState.containsKey(SCROLL_POSITION)) {
            scrollPosition = savedInstanceState.getInt(SCROLL_POSITION);
        }
        return rootView;
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
        ((ItemClickCallback) getActivity()).onItemSelected(adapter.getCursor().getLong(adapter.getCursor().getColumnIndex(
            ArticleEntry._ID)));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = ArticleEntry.COLUMN_DATE + " ASC";
        return new CursorLoader(getActivity(), ArticleEntry.CONTENT_URI, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        if (scrollPosition != ListView.INVALID_POSITION) {
            forecastList.smoothScrollToPosition(scrollPosition);
            if (((MainActivity)getActivity()).isTwoPaned()) {
                forecastList.performItemClick(rootView, scrollPosition, forecastList.getAdapter().getItemId(scrollPosition));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private void refreshWeatherData() {
        SyncAdapter.syncImmediately(getActivity());
    }
}