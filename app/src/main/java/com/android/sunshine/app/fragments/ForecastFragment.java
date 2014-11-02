package com.android.sunshine.app.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.android.sunshine.app.R;
import com.android.sunshine.app.activities.DetailActivity;
import com.android.sunshine.app.utils.WeatherRequester;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ForecastFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ArrayAdapter<String> adapter;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View view = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView forecastList = (ListView) view.findViewById(R.id.forecast_listview);
        forecastList.setOnItemClickListener(this);
        final List<String> adapterData = new ArrayList<>();
        adapterData.add("uno");
        adapterData.add("dos");
        adapter = new ArrayAdapter<>(getActivity(), R.layout.forecast_list_item, R.id.forecast_list_item, adapterData);
        forecastList.setAdapter(adapter);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            refreshWeatherData();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(DetailActivity.PlaceholderFragment.WEATHER_DATA, (String) parent.getItemAtPosition(position));
        startActivity(intent);
    }

    private void refreshWeatherData() {
        final WeatherRequester weatherRequester = new WeatherRequester();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String location = sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.location_default));
        weatherRequester.execute(location);
        try {
            adapter.clear();
            for (String s : weatherRequester.get()) {
                adapter.add(s);
            }
        } catch (InterruptedException | ExecutionException e) {
            // TODO: show error
            Log.d("AQUIIIIIIIIIII", "errrrorrrr");
            e.printStackTrace();
        }
    }
}