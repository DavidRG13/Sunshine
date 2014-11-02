package com.android.sunshine.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.*;
import android.widget.TextView;
import com.android.sunshine.app.R;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends Fragment {

        public static final String WEATHER_DATA = "weatherData";
        private String weatherData;
        private ShareActionProvider actionProvider;
        private Intent shareIntent;

        public PlaceholderFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            final Intent intent = getActivity().getIntent();
            weatherData = intent.getStringExtra(WEATHER_DATA);
            final TextView detailTextView = (TextView) rootView.findViewById(R.id.detailWeather);
            detailTextView.setText(weatherData);
            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.detail_fragment, menu);
            final MenuItem menuItem = menu.findItem(R.id.menu_item_share);
            actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            if(actionProvider != null){
                if(shareIntent == null){
                    createShareIntent();
                }
                actionProvider.setShareIntent(shareIntent);
            }
        }

        private void createShareIntent() {
            shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, weatherData + " #sunshine");
        }
    }
}