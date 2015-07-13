package com.android.sunshine.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.android.sunshine.app.App;
import com.android.sunshine.app.R;
import com.android.sunshine.app.activities.DetailActivity;
import com.android.sunshine.app.location.LocationProvider;
import com.android.sunshine.app.model.OWMWeather;
import com.android.sunshine.app.sync.WeatherRepository;
import com.android.sunshine.app.utils.DateFormatter;
import com.android.sunshine.app.utils.IntentLauncher;
import com.android.sunshine.app.utils.TemperatureFormatter;
import com.android.sunshine.app.utils.WeatherNotification;
import java.util.Locale;
import javax.inject.Inject;

public class DetailFragment extends Fragment {

    public static final String DETAIL_TRANSITION_ANIMATION = "DTA";
    public static final String DETAIL_URI = "URI";

    @Bind(R.id.detail_icon) ImageView iconView;
    @Bind(R.id.detail_date_textview) TextView dateView;
    @Bind(R.id.detail_forecast_textview) TextView mDescriptionView;
    @Bind(R.id.detail_high_textview) TextView mHighTempView;
    @Bind(R.id.detail_low_textview) TextView mLowTempView;
    @Bind(R.id.detail_humidity_textview) TextView mHumidityView;
    @Bind(R.id.detail_wind_textview) TextView mWindView;
    @Bind(R.id.detail_pressure_textview) TextView mPressureView;

    @Inject
    LocationProvider locationProvider;

    @Inject
    TemperatureFormatter temperatureFormatter;

    @Inject
    DateFormatter dateFormatter;

    @Inject
    IntentLauncher intentLauncher;

    @Inject
    WeatherRepository weatherRepository;

    private boolean transitionAnimation;
    private String weatherData;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        ((App) getActivity().getApplication()).getComponent().inject(this);
        Bundle arguments = getArguments();
        if (arguments != null) {
            transitionAnimation = arguments.getBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, false);
        }

        final View view = inflater.inflate(R.layout.fragment_detail_start, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        final long date = getArguments().getLong(IntentLauncher.DATE_KEY);
        renderData(weatherRepository.getForecastFor(date, locationProvider.getPostCode()));
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        if (getActivity() instanceof DetailActivity) {
            inflater.inflate(R.menu.detail_fragment, menu);
            finishCreatingMenu(menu);
        }
    }

    private void renderData(final WeatherNotification weather) {
        ViewParent vp = getView().getParent();
        if (vp instanceof CardView) {
            ((View) vp).setVisibility(View.VISIBLE);
        }

        String date = dateFormatter.getFullFriendlyDayString(weather.getForecastDate());
        dateView.setText(date);
        String description = weather.getDescription();
        mDescriptionView.setText(description);
        String max = weather.getMax();
        mHighTempView.setText(max);
        String min = weather.getMin();
        mLowTempView.setText(min);
        mHumidityView.setText(weather.getHumidity());
        mWindView.setText(weather.getWind());
        mPressureView.setText(weather.getPressure());
        iconView.setImageResource(OWMWeather.getArtResourceForWeatherCondition(weather.getWeatherId()));

        weatherData = String.format(Locale.getDefault(), "%s - %s - %s/%s", date, description, max, min);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);

        // We need to start the enter transition after the data has loaded
        if (transitionAnimation) {
            activity.supportStartPostponedEnterTransition();

            if (null != toolbarView) {
                activity.setSupportActionBar(toolbarView);

                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } else {
            if (null != toolbarView) {
                Menu menu = toolbarView.getMenu();
                if (null != menu) {
                    menu.clear();
                }
                toolbarView.inflateMenu(R.menu.detail_fragment);
                finishCreatingMenu(toolbarView.getMenu());
            }
        }
    }

    private void finishCreatingMenu(final Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(intentLauncher.createShareIntent(weatherData));
    }
}
