package com.android.sunshine.app.fragments

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.view.*
import android.widget.AdapterView
import android.widget.ListView
import com.android.sunshine.app.R
import com.android.sunshine.app.adapter.ForecastCursorAdapter
import com.android.sunshine.app.callbacks.ItemClickCallback
import com.android.sunshine.app.model.WeatherContract
import com.android.sunshine.app.sync.SyncAdapter
import com.android.sunshine.app.utils.Utilities

import java.util.Date

import com.android.sunshine.app.model.WeatherContract.LocationEntry
import com.android.sunshine.app.model.WeatherContract.WeatherEntry

public class ForecastFragment : Fragment(), AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    
    private var location: String? = null
    private var adapter: ForecastCursorAdapter? = null
    private var scrollPosition: Int = 0
    private var forecastList: ListView? = null
    private var rootView: View

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super<Fragment>.onActivityCreated(savedInstanceState)
        getLoaderManager().initLoader<Cursor>(FORECAST_LOADER, null, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setHasOptionsMenu(true)
        rootView = inflater.inflate(R.layout.fragment_main, container, false)
        forecastList = rootView.findViewById(R.id.forecast_listview) as ListView
        forecastList!!.setOnItemClickListener(this)
        adapter = ForecastCursorAdapter(getActivity(), null, 0)
        forecastList!!.setAdapter(adapter)
        if (savedInstanceState != null && savedInstanceState.containsKey(SCROLL_POSITION)) {
            scrollPosition = savedInstanceState.getInt(SCROLL_POSITION)
        }
        return rootView
    }

    override fun onResume() {
        super<Fragment>.onResume()
        if (location != null && !Utilities.getLocationSettings(getActivity()).equals(location)) {
            getLoaderManager().restartLoader<Cursor>(FORECAST_LOADER, null, this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, menuInflater: MenuInflater?) {
        menuInflater!!.inflate(R.menu.forecast_fragment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item!!.getItemId()
        if (itemId == R.id.action_refresh) {
            refreshWeatherData()
        } else if (itemId == R.id.viewLocation) {
            showCurrentLocation()
            return true
        }
        return super<Fragment>.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        if (scrollPosition != ListView.INVALID_POSITION) {
            outState!!.putInt(SCROLL_POSITION, scrollPosition)
        }
        super<Fragment>.onSaveInstanceState(outState)
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        this.scrollPosition = position
        (getActivity() as ItemClickCallback).onItemSelected(adapter!!.getCursor().getString(adapter!!.getCursor().getColumnIndex(WeatherEntry.COLUMN_DATETEXT)))
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Cursor> {
        val startDate = WeatherContract.getDbDateString(Date())

        val sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC"

        location = Utilities.getLocationSettings(getActivity())
        val weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(location, startDate)

        return CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        adapter!!.swapCursor(data)
        if (scrollPosition != ListView.INVALID_POSITION) {
            forecastList!!.smoothScrollToPosition(scrollPosition)
            if (!adapter!!.useTodayLayout) {
                forecastList!!.performItemClick(rootView, scrollPosition, forecastList!!.getAdapter().getItemId(scrollPosition))
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        adapter!!.swapCursor(null)
    }

    public fun setUseTodayLayout(useTodayLayout: Boolean) {
        if (adapter != null) {
            adapter!!.useTodayLayout = useTodayLayout
        }
    }

    private fun refreshWeatherData() {
        SyncAdapter.syncImmediately(getActivity())
    }

    private fun showCurrentLocation() {
        if (null != adapter) {
            val c = adapter!!.getCursor()
            if (null != c) {
                c.moveToPosition(0)
                val posLat = c.getString(c.getColumnIndex(LocationEntry.COLUMN_COORD_LAT))
                val posLong = c.getString(c.getColumnIndex(LocationEntry.COLUMN_COORD_LONG))
                val geoLocation = Uri.parse("geo:" + posLat + "," + posLong)

                System.out.println("geoLocation = " + geoLocation)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setData(geoLocation)

                startActivity(intent)
            }
        }
    }

    class object {

        public val FORECAST_LOADER: Int = 0
        public val SCROLL_POSITION: String = "scrollPosition"

        private val FORECAST_COLUMNS = array(WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID, WeatherEntry.COLUMN_DATETEXT, WeatherEntry.COLUMN_SHORT_DESC, WeatherEntry.COLUMN_MAX_TEMP, WeatherEntry.COLUMN_MIN_TEMP,
                WeatherEntry.COLUMN_WEATHER_ID, WeatherEntry.COLUMN_WEATHER_ID, LocationEntry.COLUMN_LOCATION_SETTING, LocationEntry.COLUMN_COORD_LAT, LocationEntry.COLUMN_COORD_LONG)
    }
}