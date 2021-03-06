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
import javax.inject.Inject;

public class Navigator {

    public static final String WEATHER_DETAILS = "weather_details";
    public static final String ACTION_DATA_UPDATED = "com.example.android.sunshine.app.ACTION_DATA_UPDATED";

    @Inject
    public Navigator() { }

    public void displayMapWithLocation(final Context context, final String postCode) {
        Uri geoLocation = Uri.parse("geo:" + postCode);

        System.out.println("geoLocation = " + geoLocation);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        context.startActivity(intent);
    }

    public void launchSettingsActivity(final Context context) {
        context.startActivity(new Intent(context, SettingsActivity.class));
    }

    public void transitionToDetails(final Activity activity, final WeatherDetails weatherDetails, final View sharedView) {
        final Intent intent = new Intent(activity, DetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(WEATHER_DETAILS, weatherDetails);
        intent.putExtras(bundle);
        ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
            new Pair<>(sharedView, activity.getString(R.string.detail_icon_transition_name)));
        ActivityCompat.startActivity(activity, intent, activityOptions.toBundle());
    }

    public void twoPaneDetails(final WeatherDetails weatherDetails, final AppCompatActivity fromActivity) {
        final Bundle args = new Bundle();
        args.putParcelable(WEATHER_DETAILS, weatherDetails);
        final DetailFragment fragment = new DetailFragment();
        fragment.setArguments(args);
        fromActivity.getSupportFragmentManager().beginTransaction()
            .replace(R.id.weather_detail_container, fragment)
            .commitAllowingStateLoss();
    }

    public void detailsWithTransitionEnabled(final WeatherDetails weatherDetails, final AppCompatActivity fromActivity) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(WEATHER_DETAILS, weatherDetails);
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

    public Intent displayDetailsWith(final WeatherDetails weatherDetails) {
        final Intent fillInIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable(WEATHER_DETAILS, weatherDetails);
        fillInIntent.putExtras(bundle);
        return fillInIntent;
    }

    public PendingIntent pendingToMain(final Context context) {
        Intent launchIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, 0, launchIntent, 0);
    }

    public void updateWidgets(final Context context) {
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED).setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
}
