package com.android.sunshine.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import com.android.sunshine.app.R;

public class ForecastFragment extends Fragment {

    public ForecastFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.forecast_fragment, menu);
    }
}