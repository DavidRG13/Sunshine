package com.android.sunshine.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.android.sunshine.app.R;
import com.android.sunshine.app.utils.WeatherRequester;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> adapter;
    private List<String> adapterData;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View view = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView forecastList = (ListView) view.findViewById(R.id.forecast_listview);
        adapterData = new ArrayList<>();
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

    private void refreshWeatherData() {
        final ArrayList<String> newWeatherData;
        final WeatherRequester weatherRequester = new WeatherRequester();
        weatherRequester.execute("94043");
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