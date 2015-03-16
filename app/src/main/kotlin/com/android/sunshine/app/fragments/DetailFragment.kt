package com.android.sunshine.app.fragments

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.ShareActionProvider
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.android.sunshine.app.R
import com.android.sunshine.app.activities.DetailActivity
import com.android.sunshine.app.model.WeatherContract
import com.android.sunshine.app.utils.Utilities

import java.util.Locale

public class DetailFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {
    private var location: String? = null
    private var shareIntent: Intent? = null
    private var weatherData: String? = null
    private var detailDate: TextView? = null
    private var detailDescription: TextView? = null
    private var detailMax: TextView? = null
    private var detailMin: TextView? = null
    private var detailDay: TextView? = null
    private var detailWind: TextView? = null
    private var detailPressure: TextView? = null
    private var detailHumidity: TextView? = null
    private var detailIcon: ImageView? = null

    {
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super<Fragment>.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            location = savedInstanceState.getString(LOCATION_KEY)
        }
        val arguments = getArguments()
        if (arguments != null && arguments.containsKey(DetailActivity.DATE_KEY)) {
            getLoaderManager().initLoader<Cursor>(DETAIL_LOADER, null, this)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.fragment_detail, container, false)
        detailDate = view.findViewById(R.id.detail_date) as TextView
        detailDescription = view.findViewById(R.id.detail_forecast) as TextView
        detailMax = view.findViewById(R.id.detail_max) as TextView
        detailMin = view.findViewById(R.id.detail_min) as TextView
        detailDay = view.findViewById(R.id.detail_day) as TextView
        detailWind = view.findViewById(R.id.detail_wind) as TextView
        detailPressure = view.findViewById(R.id.detail_pressure) as TextView
        detailHumidity = view.findViewById(R.id.detail_humidity) as TextView
        detailIcon = view.findViewById(R.id.detail_icon) as ImageView
        return view
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super<Fragment>.onSaveInstanceState(outState)
        if (location != null) {
            outState!!.putString(LOCATION_KEY, location)
        }
    }

    override fun onResume() {
        super.onResume()
        val arguments = getArguments()
        if (arguments != null && !location!!.equals(Utilities.getLocationSettings(getActivity())) && arguments.containsKey(DetailActivity.DATE_KEY)) {
            getLoaderManager().restartLoader<Cursor>(DETAIL_LOADER, null, this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.detail_fragment, menu)
        val menuItem = menu!!.findItem(R.id.menu_item_share)
        val actionProvider = MenuItemCompat.getActionProvider(menuItem) as ShareActionProvider
        if (shareIntent == null) {
            createShareIntent()
        }
        actionProvider.setShareIntent(shareIntent)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Cursor> {
        val date = getArguments().getString(DetailActivity.DATE_KEY)
        location = Utilities.getLocationSettings(getActivity())
        val weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, date)
        return CursorLoader(getActivity(), weatherUri, COLUMNS, null, null, WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC")
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        if (data.moveToFirst()) {
            val isMetric = Utilities.isMetric(getActivity())
            val weatherId = data.getInt(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID))
            val description = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC))
            val date = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT))
            val wind = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED))
            val pressure = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_PRESSURE))
            val humidity = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY))
            val maxTemp = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP))
            val minTemp = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP))
            val max = Utilities.formatTemperature(getActivity(), maxTemp, isMetric).toString()
            val min = Utilities.formatTemperature(getActivity(), minTemp, isMetric).toString()

            detailDate!!.setText(date)
            detailDescription!!.setText(description)
            detailMax!!.setText(max)
            detailMin!!.setText(min)
            detailHumidity!!.setText(humidity)
            detailWind!!.setText(wind)
            detailPressure!!.setText(pressure)
            detailDay!!.setText(Utilities.getDayName(getActivity(), date))
            detailIcon!!.setImageResource(Utilities.getArtResourceForWeatherCondition(weatherId))

            weatherData = String().format(Locale.getDefault(), "%s - %s - %s/%s", date, description, max, min)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
    }

    private fun createShareIntent() {
        shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        shareIntent!!.setType("text/plain")
        shareIntent!!.putExtra(Intent.EXTRA_TEXT, weatherData + " #sunshine")
    }

    class object {

        public val DETAIL_LOADER: Int = 0
        public val LOCATION_KEY: String = "location"
        private val COLUMNS = array(WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID, WeatherContract.WeatherEntry.COLUMN_DATETEXT, WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, WeatherContract.WeatherEntry.COLUMN_HUMIDITY, WeatherContract.WeatherEntry.COLUMN_PRESSURE,
                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, WeatherContract.WeatherEntry.COLUMN_WEATHER_ID)
    }
}