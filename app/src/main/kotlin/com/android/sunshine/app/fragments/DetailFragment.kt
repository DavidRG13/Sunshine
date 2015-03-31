package com.android.sunshine.app.fragments

import android.app.Fragment
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
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
import kotlinx.android.synthetic.fragment_detail.*

import java.util.Locale

public class DetailFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {

    var location: String = ""
    var shareIntent: Intent? = null
    var weatherData: String = ""

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super<Fragment>.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            location = savedInstanceState.getString(LOCATION_KEY, "")
        }
        val arguments = getArguments()
        if (arguments != null && arguments.containsKey(DetailActivity().DATE_KEY)) {
            getLoaderManager().initLoader<Cursor>(DETAIL_LOADER, null, this)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_detail, container, false)
        return view
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super<Fragment>.onSaveInstanceState(outState)
        if (location.isNotEmpty()) {
            outState!!.putString(LOCATION_KEY, location)
        }
    }

    override fun onResume() {
        super<Fragment>.onResume()
        val arguments = getArguments()
        if (arguments != null && !location.equals(Utilities.getLocationSettings(getActivity())) && arguments.containsKey(DetailActivity().DATE_KEY)) {
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
        val date = getArguments().getString(DetailActivity().DATE_KEY)
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

            detail_date.setText(date)
            detail_forecast!!.setText(description)
            detail_max!!.setText(max)
            detail_min!!.setText(min)
            detail_humidity!!.setText(humidity)
            detail_wind!!.setText(wind)
            detail_pressure!!.setText(pressure)
            detail_day.setText(Utilities.getDayName(getActivity(), date))
            detail_icon!!.setImageResource(Utilities.getArtResourceForWeatherCondition(weatherId))

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

    companion object {

        public val DETAIL_LOADER: Int = 0
        public val LOCATION_KEY: String = "location"
        private val COLUMNS = array(WeatherContract.WeatherEntry.TABLE_NAME + "._id", WeatherContract.WeatherEntry.COLUMN_DATETEXT, WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, WeatherContract.WeatherEntry.COLUMN_HUMIDITY, WeatherContract.WeatherEntry.COLUMN_PRESSURE,
                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, WeatherContract.WeatherEntry.COLUMN_WEATHER_ID)
    }
}