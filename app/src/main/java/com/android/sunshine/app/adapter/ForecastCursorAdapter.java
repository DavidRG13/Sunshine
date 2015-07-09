package com.android.sunshine.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.android.sunshine.app.R;
import com.android.sunshine.app.model.OWMWeather;
import com.android.sunshine.app.utils.ApplicationPreferences;
import com.android.sunshine.app.utils.DateFormatter;
import com.android.sunshine.app.utils.TemperatureFormatter;

import static com.android.sunshine.app.model.WeatherContract.WeatherEntry;

public class ForecastCursorAdapter extends RecyclerView.Adapter<ForecastCursorAdapter.ViewHolder> implements OnItemClickHandler {

    private static final int TODAY_VIEW_TYPE = 0;
    private static final int FUTURE_DAY_VIEW_TYPE = 1;

    private Cursor cursor;
    private Context context;
    private View emptyView;
    private OnAdapterItemClickListener onAdapterItemClickListener;
    private final TemperatureFormatter temperatureFormatter;
    private final DateFormatter dateFormatter;
    private final ApplicationPreferences applicationPreferences;
    private final ItemChoiceManager itemChoiceManager;

    public ForecastCursorAdapter(final Context context, final View emptyView, final OnAdapterItemClickListener onAdapterItemClickListener,
        final int choiceMode, final TemperatureFormatter temperatureFormatter, final DateFormatter dateFormatter, final ApplicationPreferences applicationPreferences) {
        this.context = context;
        this.emptyView = emptyView;
        this.onAdapterItemClickListener = onAdapterItemClickListener;
        this.temperatureFormatter = temperatureFormatter;
        this.dateFormatter = dateFormatter;
        this.applicationPreferences = applicationPreferences;
        itemChoiceManager = new ItemChoiceManager(this);
        itemChoiceManager.setChoiceMode(choiceMode);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int viewType) {
        if (viewGroup instanceof RecyclerView) {
            int layoutId = -1;
            switch (viewType) {
                case TODAY_VIEW_TYPE:
                    layoutId = R.layout.today_list_item;
                    break;
                case FUTURE_DAY_VIEW_TYPE:
                    layoutId = R.layout.forecast_list_item;
                    break;
                default:
                    break;
            }
            final View view = LayoutInflater.from(context).inflate(layoutId, viewGroup, false);
            return new ViewHolder(view, this);
        } else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        cursor.moveToPosition(position);

        final int weatherId = cursor.getInt(cursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID));
        final long weatherDate = cursor.getLong(cursor.getColumnIndex(WeatherEntry.COLUMN_DATE));
        final String descriptionWeather = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
        final float maxTemp = cursor.getFloat(cursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP));
        final float minTemp = cursor.getFloat(cursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP));

        boolean useLongToday;
        if (getItemViewType(cursor.getPosition()) == TODAY_VIEW_TYPE) {
            useLongToday = true;
            viewHolder.forecastIcon.setImageResource(OWMWeather.getArtResourceForWeatherCondition(weatherId));
        } else {
            useLongToday = false;
            viewHolder.forecastIcon.setImageResource(OWMWeather.getIconResourceForWeatherCondition(weatherId));
        }

        viewHolder.dateWeather.setText(dateFormatter.getFriendlyDay(weatherDate, useLongToday));
        viewHolder.forecastDescription.setText(descriptionWeather);
        viewHolder.max.setText(temperatureFormatter.format(maxTemp));
        viewHolder.min.setText(temperatureFormatter.format(minTemp));
        ViewCompat.setTransitionName(viewHolder.forecastIcon, "iconView" + position);

        itemChoiceManager.onBindViewHolder(viewHolder, position);
    }

    @Override
    public int getItemViewType(final int position) {
        return (position == 0 && applicationPreferences.useTodayLayout()) ? TODAY_VIEW_TYPE : FUTURE_DAY_VIEW_TYPE;
    }

    @Override
    public int getItemCount() {
        if (null == cursor) {
            return 0;
        } else {
            return cursor.getCount();
        }
    }

    public void swapCursor(final Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
        emptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return cursor;
    }

    @Override
    public void itemClick(final RecyclerView.ViewHolder viewHolder) {
        int position = viewHolder.getAdapterPosition();
        cursor.moveToPosition(position);
        long date = cursor.getLong(cursor.getColumnIndex(WeatherEntry.COLUMN_DATE));
        onAdapterItemClickListener.onClick(date, (ViewHolder) viewHolder);
        itemChoiceManager.onClick(viewHolder);
    }

    public void selectView(final RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) viewHolder;
            holder.onClick(holder.itemView);
        }
    }

    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        itemChoiceManager.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(final Bundle outState) {
        itemChoiceManager.onSaveInstanceState(outState);
    }

    public int getSelectedItemPosition() {
        return itemChoiceManager.getSelectedItemPosition();
    }

    public static final class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.list_item_icon) public ImageView forecastIcon;
        @Bind(R.id.list_item_date_textview) public TextView dateWeather;
        @Bind(R.id.list_item_forecast_textview) public TextView forecastDescription;
        @Bind(R.id.list_item_high_textview) public TextView max;
        @Bind(R.id.list_item_low_textview) public TextView min;
        private final OnItemClickHandler onItemClickHandler;

        private ViewHolder(final View view, final OnItemClickHandler onItemClickHandler) {
            super(view);
            ButterKnife.bind(this, view);
            this.onItemClickHandler = onItemClickHandler;
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(final View view) {
            onItemClickHandler.itemClick(this);
        }
    }
}
