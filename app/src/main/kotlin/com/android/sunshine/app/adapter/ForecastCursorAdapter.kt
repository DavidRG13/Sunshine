package com.android.sunshine.app.adapter

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import com.android.sunshine.app.R
import com.android.sunshine.app.utils.Utilities

import com.android.sunshine.app.model.WeatherContract.WeatherEntry

public class ForecastCursorAdapter(context: Context, c: Cursor, flags: Int) : CursorAdapter(context, c, flags) {
    public var useTodayLayout: Boolean = false

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        val itemViewType = getItemViewType(cursor.getPosition())
        var layoutId = -1
        when (itemViewType) {
            TODAY_VIEW_TYPE -> layoutId = R.layout.today_list_item
            FUTURE_DAY_VIEW_TYPE -> layoutId = R.layout.forecast_list_item
        }
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        val viewHolder = ViewHolder(view)
        view.setTag(viewHolder)
        return view
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val isMetric = Utilities.isMetric(context)
        val weatherId = cursor.getInt(cursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID))
        val weatherDate = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_DATETEXT))
        val descriptionWeather = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC))
        val maxTemp = cursor.getFloat(cursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP))
        val minTemp = cursor.getFloat(cursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP))

        val viewHolder = view.getTag() as ViewHolder

        viewHolder.dateWeather.setText(Utilities.getFriendlyDay(context, weatherDate))
        viewHolder.forecastDescription.setText(descriptionWeather)
        viewHolder.max.setText(Utilities.formatTemperature(context, maxTemp.toDouble(), isMetric))
        viewHolder.min.setText(Utilities.formatTemperature(context, minTemp.toDouble(), isMetric))

        if (getItemViewType(cursor.getPosition()) == TODAY_VIEW_TYPE) {
            viewHolder.forecastIcon.setImageResource(Utilities.getArtResourceForWeatherCondition(weatherId))
        } else {
            viewHolder.forecastIcon.setImageResource(Utilities.getIconResourceForWeatherCondition(weatherId))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if ((position == 0 && useTodayLayout)) TODAY_VIEW_TYPE else FUTURE_DAY_VIEW_TYPE
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    private class ViewHolder public(view: View) {
        public val forecastIcon: ImageView
        public val dateWeather: TextView
        public val forecastDescription: TextView
        public val max: TextView
        public val min: TextView

        {
            forecastIcon = view.findViewById(R.id.list_item_icon) as ImageView
            dateWeather = view.findViewById(R.id.list_item_date) as TextView
            forecastDescription = view.findViewById(R.id.list_item_forecast) as TextView
            max = view.findViewById(R.id.list_item_max) as TextView
            min = view.findViewById(R.id.list_item_min) as TextView
        }
    }

    class object {
        private val TODAY_VIEW_TYPE = 0
        private val FUTURE_DAY_VIEW_TYPE = 1
    }
}