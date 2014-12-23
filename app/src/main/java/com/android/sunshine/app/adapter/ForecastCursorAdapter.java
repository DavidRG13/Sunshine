package com.android.sunshine.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.sunshine.app.R;
import com.android.sunshine.app.utils.BitmapUtils;

import static com.android.sunshine.app.model.Contract.ArticleEntry;

public class ForecastCursorAdapter extends CursorAdapter {

    private static final int TODAY_VIEW_TYPE = 0;
    private static final int FUTURE_DAY_VIEW_TYPE = 1;
    private boolean useTodayLayout;

    public ForecastCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final int itemViewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (itemViewType) {
            case TODAY_VIEW_TYPE:
                layoutId = R.layout.today_list_item;
                break;
            case FUTURE_DAY_VIEW_TYPE:
                layoutId = R.layout.forecast_list_item;
                break;
        }
        final View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final String date = cursor.getString(cursor.getColumnIndex(ArticleEntry.COLUMN_DATE));
        final String descriptionWeather =
            cursor.getString(cursor.getColumnIndex(ArticleEntry.COLUMN_SHORT_DESCRIPTION));
        final String section =
            cursor.getString(cursor.getColumnIndex(ArticleEntry.COLUMN_SECTION_NAME));

        String url = cursor.getString(cursor.getColumnIndex(ArticleEntry.COLUMN_THUMBNAIL));
        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (getItemViewType(cursor.getPosition()) == TODAY_VIEW_TYPE) {
            viewHolder.articleDescription.setText(descriptionWeather);
            displayImage(url, viewHolder.articleThumbnail, R.drawable.art_clear);
        } else {
            displayImage(url, viewHolder.articleThumbnail, R.drawable.art_clear);
            //viewHolder.articleDate.setText(Utilities.getFriendlyDay(context, date));
            viewHolder.articleDate.setText(date);
            viewHolder.articleDescription.setText(descriptionWeather);
            viewHolder.section.setText(section);
        }
    }

    private void displayImage(String url, ImageView imageView, int defaultBitmapResource) {
        if (url == null) {
            imageView.setImageResource(defaultBitmapResource);
        } else {
            BitmapUtils.displayImageIn(imageView, url);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && useTodayLayout) ? TODAY_VIEW_TYPE : FUTURE_DAY_VIEW_TYPE;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.useTodayLayout = useTodayLayout;
    }

    public boolean getUseTodayLayout() {
        return useTodayLayout;
    }

    private static class ViewHolder {
        public final ImageView articleThumbnail;
        public final TextView articleDate;
        public final TextView articleDescription;
        public final TextView section;

        private ViewHolder(View view) {
            articleThumbnail = (ImageView) view.findViewById(R.id.list_item_icon);
            articleDate = (TextView) view.findViewById(R.id.list_item_date);
            articleDescription = (TextView) view.findViewById(R.id.list_item_desc);
            section = (TextView) view.findViewById(R.id.list_item_section);
        }
    }
}