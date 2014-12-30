package com.android.sunshine.app.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.android.sunshine.app.R;
import com.android.sunshine.app.activities.MainActivity;
import com.android.sunshine.app.adapter.ArticlesCursorAdapter;
import com.android.sunshine.app.callbacks.ItemClickCallback;
import com.android.sunshine.app.sync.SyncAdapter;

import static com.android.sunshine.app.model.Contract.ArticleEntry;

public class ArticlesFragment extends Fragment implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER = 0;
    public static final String SCROLL_POSITION = "scrollPosition";
    private ArticlesCursorAdapter adapter;

    private static final String[] COLUMNS = new String[]{
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
    private ListView listView;
    private View rootView;

    public ArticlesFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER, null, this);
        refreshData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        listView = (ListView) rootView.findViewById(R.id.article_listview);
        listView.setOnItemClickListener(this);
        adapter = new ArticlesCursorAdapter(getActivity(), null, 0);
        listView.setAdapter(adapter);
        if (savedInstanceState != null && savedInstanceState.containsKey(SCROLL_POSITION)) {
            scrollPosition = savedInstanceState.getInt(SCROLL_POSITION);
        }
        return rootView;
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
        return new CursorLoader(getActivity(), ArticleEntry.CONTENT_URI, COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        if (scrollPosition != ListView.INVALID_POSITION) {
            listView.smoothScrollToPosition(scrollPosition);
            if (((MainActivity)getActivity()).isTwoPaned() && data.getCount() > 0) {
                listView.performItemClick(rootView, scrollPosition,
                    listView.getAdapter().getItemId(scrollPosition));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private void refreshData() {
        SyncAdapter.syncImmediately(getActivity());
    }
}