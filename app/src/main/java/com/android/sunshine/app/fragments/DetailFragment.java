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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.sunshine.app.R;
import com.android.sunshine.app.activities.DetailActivity;
import com.android.sunshine.app.utils.BitmapUtils;
import com.android.sunshine.app.utils.Utilities;

import static com.android.sunshine.app.model.Contract.ArticleEntry;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener,
    ShareActionProvider.OnShareTargetSelectedListener {

    public static final int DETAIL_LOADER = 0;
    private Intent shareIntent;
    private static final String[] COLUMNS = new String[] {
        ArticleEntry.TABLE_NAME + "." + ArticleEntry._ID, ArticleEntry.COLUMN_DATE,
        ArticleEntry.COLUMN_LARGE_IMAGE, ArticleEntry.COLUMN_SECTION_NAME,
        ArticleEntry.COLUMN_SHORT_DESCRIPTION, ArticleEntry.COLUMN_SNIPPET,
        ArticleEntry.COLUMN_THUMBNAIL, ArticleEntry.COLUMN_URL,
    };
    private ImageView detailImage;
    private TextView detailDescription;
    private TextView detailSection;
    private TextView detailDate;
    private TextView detailSnippet;
    private String url;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(DetailActivity.ID_KEY)) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_detail, container, false);
        detailDate = (TextView) view.findViewById(R.id.detail_date);
        detailDescription = (TextView) view.findViewById(R.id.detail_description);
        detailSnippet = (TextView) view.findViewById(R.id.detail_snippet);
        detailSection = (TextView) view.findViewById(R.id.detail_section);
        detailImage = (ImageView) view.findViewById(R.id.detail_image);
        Button detailLink = (Button) view.findViewById(R.id.detail_link);
        detailLink.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        final Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(DetailActivity.ID_KEY)) {
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
            actionProvider.setOnShareTargetSelectedListener(this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final long articleId = getArguments().getLong(DetailActivity.ID_KEY);
        final Uri weatherUri = ArticleEntry.buildWeatherUri(articleId);
        return new CursorLoader(getActivity(), weatherUri, COLUMNS, null, null, ArticleEntry.COLUMN_DATE + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            final String description = data.getString(data.getColumnIndex(ArticleEntry.COLUMN_SHORT_DESCRIPTION));
            final String date = data.getString(data.getColumnIndex(ArticleEntry.COLUMN_DATE));
            final String section = data.getString(data.getColumnIndex(ArticleEntry.COLUMN_SECTION_NAME));
            final String snippet = data.getString(data.getColumnIndex(ArticleEntry.COLUMN_SNIPPET));
            url = data.getString(data.getColumnIndex(ArticleEntry.COLUMN_URL));
            final String thumbnail = data.getString(data.getColumnIndex(ArticleEntry.COLUMN_THUMBNAIL));
            final String largeImage = data.getString(data.getColumnIndex(ArticleEntry.COLUMN_LARGE_IMAGE));

            detailDate.setText(Utilities.getFriendlyDay(getActivity(), date));
            detailDescription.setText(description);
            if (!"null".equals(section)) {
                detailSection.setText(section);
            }
            detailSnippet.setText(snippet);
            if (largeImage == null) {
                BitmapUtils.displayImage(thumbnail, detailImage, R.drawable.ic_default_article);
            } else {
                BitmapUtils.displayImage(largeImage, detailImage, R.drawable.ic_default_article);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void createShareIntent() {
        shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url + " #NYTReader");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.detail_link) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }

    @Override
    public boolean onShareTargetSelected(ShareActionProvider shareActionProvider, Intent intent) {
        createShareIntent();
        shareActionProvider.setShareIntent(shareIntent);
        return false;
    }
}