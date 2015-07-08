package com.android.sunshine.app.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.android.sunshine.app.R;
import com.android.sunshine.app.activities.DetailActivity;
import com.android.sunshine.app.activities.MainActivity;
import com.android.sunshine.app.activities.SettingsActivity;
import com.android.sunshine.app.fragments.DetailFragment;
import com.android.sunshine.app.location.LatLong;
import com.android.sunshine.app.model.WeatherContract;
import javax.inject.Inject;

public class IntentLauncher {

    public static final String DATE_KEY = "forecast_date";

    @Inject
    public IntentLauncher() {
    }

    public void displayMapWithLocation(final Context context, final LatLong latLong) {
        Uri geoLocation = Uri.parse("geo:" + latLong.getLatitude() + "," + latLong.getLongitude());

        System.out.println("geoLocation = " + geoLocation);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        context.startActivity(intent);
    }

    public void launchSettingsActivity(final Context context) {
        context.startActivity(new Intent(context, SettingsActivity.class));
    }

    public void transitionToDetails(final Activity activity, final long date, final View sharedView) {
        final Intent intent = new Intent(activity, DetailActivity.class);
        intent.putExtra(DATE_KEY, date);
        ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
            new Pair<>(sharedView, activity.getString(R.string.detail_icon_transition_name)));
        ActivityCompat.startActivity(activity, intent, activityOptions.toBundle());
    }

    public void twoPaneDetails(final long date, final AppCompatActivity fromActivity) {
        final Bundle args = new Bundle();
        args.putLong(DATE_KEY, date);
        final DetailFragment detailFragment = new DetailFragment();
        detailFragment.setArguments(args);
        fromActivity.getSupportFragmentManager().beginTransaction()
            .replace(R.id.weather_detail_container, detailFragment)
            .commitAllowingStateLoss();
    }

    public void displayTwoPaneDetails(final Uri contentUri, final AppCompatActivity fromActivity) {
        DetailFragment fragment = new DetailFragment();
        if (contentUri != null) {
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);
            fragment.setArguments(args);
        }
        fromActivity.getSupportFragmentManager().beginTransaction()
            .replace(R.id.weather_detail_container, new DetailFragment())
            .commit();
    }

    public void detailsWithTransitionEnabled(final long date, final AppCompatActivity fromActivity) {
        final Bundle bundle = new Bundle();
        bundle.putLong(DATE_KEY, date);
        bundle.putBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, true);
        final DetailFragment detailFragment = new DetailFragment();
        detailFragment.setArguments(bundle);
        fromActivity.getSupportFragmentManager().beginTransaction()
            .add(R.id.fragment_detail_container, detailFragment)
            .commit();
        fromActivity.supportPostponeEnterTransition();
    }

    public Intent createShareIntent(final String weatherData) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, weatherData + " #sunshine");
        return shareIntent;
    }

    public Intent displayDetailsWith(final String postCode, final long dateInMillis) {
        final Intent fillInIntent = new Intent();
        Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(postCode, dateInMillis);
        fillInIntent.setData(weatherUri);
        return fillInIntent;
    }

    public PendingIntent pendingToMain(final Context context) {
        Intent launchIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, 0, launchIntent, 0);
    }
}
